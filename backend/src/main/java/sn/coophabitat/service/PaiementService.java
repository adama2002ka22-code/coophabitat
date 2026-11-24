package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.payment.PaymentGateway;
import sn.coophabitat.payment.PaymentGateway.ChargeRequest;
import sn.coophabitat.payment.PaymentGateway.ChargeResult;
import sn.coophabitat.repository.CotisationRepository;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.repository.PaiementRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class PaiementService {
    private final PaiementRepository paiements;
    private final MembreRepository membres;
    private final CotisationRepository cotisations;
    private final CotisationService cotisationService;
    private final PaymentGateway gateway;
    private final NotificationService notifications;
    private final AuditService audit;
    private final Clock clock;

    public PaiementService(PaiementRepository paiements, MembreRepository membres, CotisationRepository cotisations,
                           CotisationService cotisationService, PaymentGateway gateway,
                           NotificationService notifications, AuditService audit, Clock clock) {
        this.paiements = paiements; this.membres = membres; this.cotisations = cotisations;
        this.cotisationService = cotisationService; this.gateway = gateway; this.notifications = notifications;
        this.audit = audit; this.clock = clock;
    }

    private Membre membre(Long id) {
        return membres.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
    }

    private List<Cotisation> dues(Long membreId) {
        cotisationService.rafraichirPour(membreId);
        return cotisations.findByMembreIdOrderByDateEcheanceAsc(membreId).stream()
                .filter(c -> c.getSolde().signum() > 0).toList();
    }

    private void verifierMontant(Long membreId, BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant invalide");
        List<Cotisation> dues = dues(membreId);
        if (dues.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucune cotisation a regler");
        BigDecimal total = dues.stream().map(Cotisation::getSolde).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (montant.compareTo(total) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant depasse le solde restant (" + total.toPlainString() + " FCFA)");
    }

    /** Enregistre un paiement valide et l'impute sur les echeances, de la plus ancienne a la plus recente. */
    @Transactional
    public PaiementDto enregistrer(Long membreId, BigDecimal montant, CanalPaiement canal, String reference, String acteur) {
        Membre m = membre(membreId);
        verifierMontant(membreId, montant);
        String ref = (reference == null || reference.isBlank()) ? "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : reference.trim();
        if (paiements.existsByReference(ref)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Reference de paiement deja utilisee");

        BigDecimal reste = montant;
        for (Cotisation c : dues(membreId)) {
            if (reste.signum() <= 0) break;
            BigDecimal part = reste.min(c.getSolde());
            c.setMontantPaye(c.getMontantPaye().add(part));
            reste = reste.subtract(part);
        }
        Paiement p = new Paiement();
        p.setMembre(m);
        p.setMontant(montant);
        p.setCanal(canal);
        p.setReference(ref);
        p.setDatePaiement(LocalDateTime.now(clock));
        p.setEnregistrePar(acteur);
        p = paiements.save(p);
        p.setRecuNumero(String.format("REC-%d-%06d", Year.now().getValue(), p.getId()));
        audit.log(acteur, "PAIEMENT_ENREGISTRE", "Paiement", p.getId(), m.getNumeroAdherent() + " : " + montant + " FCFA via " + canal);
        notifications.notifier(m, "Paiement enregistre", "Votre paiement de " + montant.toPlainString() + " FCFA a ete enregistre (recu " + p.getRecuNumero() + ").");
        return Mapper.paiement(p);
    }

    /** Paiement Mobile Money (Wave / SendFlash sandbox) par le membre connecte. */
    @Transactional
    public PaiementDto mobileMoney(Membre m, MobileMoneyRequest req) {
        verifierMontant(m.getId(), req.montant());
        ChargeResult r = gateway.charger(new ChargeRequest(req.telephone(), req.montant(), req.operateur()));
        if (!r.succes()) throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, r.message());
        CanalPaiement canal = "SENDFLASH".equalsIgnoreCase(req.operateur()) ? CanalPaiement.SENDFLASH : CanalPaiement.WAVE;
        return enregistrer(m.getId(), req.montant(), canal, r.reference(), m.getEmail());
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> recents() { return paiements.findTop200ByOrderByDatePaiementDesc().stream().map(Mapper::paiement).toList(); }

    @Transactional(readOnly = true)
    public List<PaiementDto> duMembre(Long membreId) {
        return paiements.findByMembreIdOrderByDatePaiementDesc(membreId).stream().map(Mapper::paiement).toList();
    }

    @Transactional(readOnly = true)
    public Paiement get(Long id) {
        return paiements.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement introuvable"));
    }
}
