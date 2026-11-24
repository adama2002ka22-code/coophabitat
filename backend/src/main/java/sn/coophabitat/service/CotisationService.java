package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.CotisationRepository;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.repository.PaiementRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CotisationService {
    private final CotisationRepository cotisations;
    private final MembreRepository membres;
    private final PaiementRepository paiements;
    private final PlanCotisationService plans;
    private final PenaltyCalculator calculator;
    private final NotificationService notifications;
    private final Clock clock;

    public CotisationService(CotisationRepository cotisations, MembreRepository membres, PaiementRepository paiements,
                             PlanCotisationService plans, PenaltyCalculator calculator,
                             NotificationService notifications, Clock clock) {
        this.cotisations = cotisations; this.membres = membres; this.paiements = paiements; this.plans = plans;
        this.calculator = calculator; this.notifications = notifications; this.clock = clock;
    }

    private boolean creer(Membre m, YearMonth ym, PlanCotisation plan) {
        LocalDate echeance = ym.atDay(Math.min(plan.getJourEcheance(), ym.lengthOfMonth()));
        if (echeance.isBefore(m.getDateAdhesion())) return false;
        if (cotisations.existsByMembreIdAndDateEcheance(m.getId(), echeance)) return false;
        Cotisation c = new Cotisation();
        c.setMembre(m);
        c.setDateEcheance(echeance);
        c.setMontantDu(plan.getMontantMensuel());
        cotisations.save(c);
        return true;
    }

    /** Genere (idempotent) les echeances du mois pour tous les membres actifs. */
    @Transactional
    public int genererEcheances(YearMonth ym) {
        PlanCotisation plan = plans.actif();
        int n = 0;
        for (Membre m : membres.findByStatut(StatutMembre.ACTIF)) if (creer(m, ym, plan)) n++;
        return n;
    }

    /** Echeances du mois courant et du suivant pour un membre (inscription). */
    @Transactional
    public void genererPour(Membre m) {
        PlanCotisation plan = plans.actif();
        YearMonth now = YearMonth.now(clock);
        creer(m, now, plan);
        creer(m, now.plusMonths(1), plan);
    }

    private void rafraichir(Cotisation c, PlanCotisation plan, LocalDate today) {
        if (c.getMontantPaye().compareTo(c.getMontantDu()) >= 0) return; // principal solde : penalite figee
        if (!c.getDateEcheance().isBefore(today)) return;
        c.setPenalite(calculator.calculer(c.getMontantDu(), c.getDateEcheance(), today, plan));
    }

    @Transactional
    public void rafraichirPenalites() {
        PlanCotisation plan = plans.actif();
        LocalDate today = LocalDate.now(clock);
        cotisations.findPrincipalImpayeEchu(today).forEach(c -> rafraichir(c, plan, today));
    }

    @Transactional
    public void rafraichirPour(Long membreId) {
        PlanCotisation plan = plans.actif();
        LocalDate today = LocalDate.now(clock);
        cotisations.findByMembreIdOrderByDateEcheanceAsc(membreId).forEach(c -> rafraichir(c, plan, today));
    }

    @Transactional(readOnly = true)
    public Set<Long> membresEnRetardIds() {
        return cotisations.findEnRetard(LocalDate.now(clock)).stream()
                .map(c -> c.getMembre().getId()).collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean estEnRetard(Long membreId) {
        return cotisations.countEnRetard(membreId, LocalDate.now(clock)) > 0;
    }

    @Transactional
    public Situation situation(Long membreId) {
        Membre m = membres.findById(membreId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
        rafraichirPour(membreId);
        LocalDate today = LocalDate.now(clock);
        BigDecimal exigible = BigDecimal.ZERO, penalites = BigDecimal.ZERO;
        LocalDate prochaine = null;
        boolean retard = false;
        List<EcheanceDto> calendrier = new ArrayList<>();
        for (Cotisation c : cotisations.findByMembreIdOrderByDateEcheanceAsc(membreId)) {
            BigDecimal solde = c.getSolde().max(BigDecimal.ZERO);
            if (!c.getDateEcheance().isAfter(today)) exigible = exigible.add(solde);
            if (c.getDateEcheance().isBefore(today) && solde.signum() > 0) retard = true;
            if (!c.getDateEcheance().isBefore(today) && solde.signum() > 0 && prochaine == null) prochaine = c.getDateEcheance();
            penalites = penalites.add(c.getPenalite());
            calendrier.add(Mapper.echeance(c, today));
        }
        List<PaiementDto> hist = paiements.findByMembreIdOrderByDatePaiementDesc(membreId).stream().map(Mapper::paiement).toList();
        return new Situation(m.getId(), m.getNom(), m.getNumeroAdherent(), Mapper.nz(paiements.totalPourMembre(membreId)),
                exigible, penalites, retard ? "EN_RETARD" : "A_JOUR", prochaine, calendrier, hist);
    }

    @Transactional
    public List<RetardataireDto> retardataires() {
        rafraichirPenalites();
        LocalDate today = LocalDate.now(clock);
        Map<Long, List<Cotisation>> parMembre = cotisations.findEnRetard(today).stream()
                .collect(Collectors.groupingBy(c -> c.getMembre().getId()));
        List<RetardataireDto> out = new ArrayList<>();
        parMembre.forEach((id, list) -> {
            Membre m = list.get(0).getMembre();
            BigDecimal solde = list.stream().map(Cotisation::getSolde).reduce(BigDecimal.ZERO, BigDecimal::add);
            long jours = list.stream().mapToLong(c -> ChronoUnit.DAYS.between(c.getDateEcheance(), today)).max().orElse(0);
            out.add(new RetardataireDto(id, m.getNom(), m.getNumeroAdherent(), m.getTelephone(), solde, jours));
        });
        out.sort(Comparator.comparing(RetardataireDto::soldeRestantDu).reversed());
        return out;
    }

    /** Rappel automatique 3 jours avant l'echeance (SMS / e-mail / in-app). Idempotent grace a rappelEnvoye. */
    @Transactional
    public int envoyerRappels() {
        LocalDate cible = LocalDate.now(clock).plusDays(3);
        int n = 0;
        for (Cotisation c : cotisations.findByDateEcheanceAndRappelEnvoyeFalse(cible)) {
            if (c.getSolde().signum() <= 0) continue;
            notifications.notifier(c.getMembre(), "Rappel d'echeance de cotisation",
                    "Bonjour " + c.getMembre().getNom() + ", votre cotisation de "
                            + c.getSolde().setScale(0, RoundingMode.HALF_UP).toPlainString()
                            + " FCFA est due le " + c.getDateEcheance() + " (dans 3 jours).");
            c.setRappelEnvoye(true);
            n++;
        }
        return n;
    }
}
