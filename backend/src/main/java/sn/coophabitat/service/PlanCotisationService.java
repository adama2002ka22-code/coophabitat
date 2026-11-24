package sn.coophabitat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.coophabitat.domain.PlanCotisation;
import sn.coophabitat.dto.Dtos.PlanRequest;
import sn.coophabitat.repository.PlanCotisationRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
public class PlanCotisationService {
    private final PlanCotisationRepository repo;
    private final AuditService audit;
    private final Clock clock;
    private final BigDecimal montant, taux, fixe;
    private final int jour, grace;

    public PlanCotisationService(PlanCotisationRepository repo, AuditService audit, Clock clock,
                                 @Value("${app.plan.montant}") BigDecimal montant, @Value("${app.plan.jour}") int jour,
                                 @Value("${app.plan.taux}") BigDecimal taux, @Value("${app.plan.fixe}") BigDecimal fixe,
                                 @Value("${app.plan.grace}") int grace) {
        this.repo = repo; this.audit = audit; this.clock = clock;
        this.montant = montant; this.jour = jour; this.taux = taux; this.fixe = fixe; this.grace = grace;
    }

    @Transactional
    public PlanCotisation actif() {
        return repo.findFirstByActifTrueOrderByIdDesc().orElseGet(() ->
                repo.save(build(montant, jour, taux, fixe, grace)));
    }

    @Transactional
    public PlanCotisation definir(PlanRequest r, String acteur) {
        repo.findByActifTrue().forEach(p -> p.setActif(false));
        PlanCotisation p = repo.save(build(r.montantMensuel(), r.jourEcheance(), r.tauxPenaliteJournalier(),
                r.penaliteFixe(), r.delaiGraceJours()));
        audit.log(acteur, "PLAN_COTISATION_MODIFIE", "PlanCotisation", p.getId(),
                "montant=" + r.montantMensuel() + ", jour=" + r.jourEcheance() + ", taux=" + r.tauxPenaliteJournalier()
                        + "%/jour, fixe=" + r.penaliteFixe() + ", grace=" + r.delaiGraceJours() + "j");
        return p;
    }

    private PlanCotisation build(BigDecimal m, int j, BigDecimal t, BigDecimal f, int g) {
        PlanCotisation p = new PlanCotisation();
        p.setMontantMensuel(m); p.setJourEcheance(j); p.setTauxPenaliteJournalier(t);
        p.setPenaliteFixe(f); p.setDelaiGraceJours(g); p.setActif(true); p.setDateEffet(LocalDate.now(clock));
        return p;
    }
}
