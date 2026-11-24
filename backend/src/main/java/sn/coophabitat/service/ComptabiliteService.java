package sn.coophabitat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.coophabitat.domain.BudgetLigne;
import sn.coophabitat.domain.Depense;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.domain.StatutMembre;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.BudgetLigneRepository;
import sn.coophabitat.repository.DepenseRepository;
import sn.coophabitat.repository.MembreRepository;
import sn.coophabitat.repository.PaiementRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ComptabiliteService {
    private final PaiementRepository paiements;
    private final DepenseRepository depenses;
    private final BudgetLigneRepository budgets;
    private final MembreRepository membres;
    private final CotisationService cotisationService;
    private final AuditService audit;
    private final Clock clock;

    public ComptabiliteService(PaiementRepository paiements, DepenseRepository depenses, BudgetLigneRepository budgets,
                               MembreRepository membres, CotisationService cotisationService, AuditService audit, Clock clock) {
        this.paiements = paiements; this.depenses = depenses; this.budgets = budgets; this.membres = membres;
        this.cotisationService = cotisationService; this.audit = audit; this.clock = clock;
    }

    /** Sequestre du fonds commun : visible par tous les membres. */
    @Transactional(readOnly = true)
    public TresorerieDto tresorerie() {
        BigDecimal in = Mapper.nz(paiements.totalEncaisse());
        BigDecimal out = Mapper.nz(depenses.totalDepenses());
        return new TresorerieDto(in, out, in.subtract(out));
    }

    @Transactional
    public DashboardDto dashboard() {
        List<Membre> actifs = membres.findByStatut(StatutMembre.ACTIF);
        Set<Long> retards = cotisationService.membresEnRetardIds();
        long enRetard = actifs.stream().filter(m -> retards.contains(m.getId())).count();
        List<RetardataireDto> retardataires = cotisationService.retardataires();
        BigDecimal impayes = retardataires.stream().map(RetardataireDto::soldeRestantDu).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardDto(actifs.size(), actifs.size() - enRetard, enRetard, impayes, tresorerie(), retardataires);
    }

    @Transactional(readOnly = true)
    public List<BudgetLigneDto> budget(int exercice) {
        return budgets.findByExerciceOrderByCategorieAsc(exercice).stream().map(Mapper::budget).toList();
    }

    @Transactional
    public BudgetLigneDto ajouterBudget(BudgetLigneRequest r, String acteur) {
        BudgetLigne b = new BudgetLigne();
        b.setExercice(r.exercice()); b.setCategorie(r.categorie()); b.setLibelle(r.libelle()); b.setMontantPrevu(r.montantPrevu());
        b = budgets.save(b);
        audit.log(acteur, "BUDGET_LIGNE_AJOUTEE", "BudgetLigne", b.getId(), r.categorie() + " : " + r.montantPrevu());
        return Mapper.budget(b);
    }

    @Transactional(readOnly = true)
    public List<DepenseDto> depenses() { return depenses.findAllByOrderByDateDepenseDesc().stream().map(Mapper::depense).toList(); }

    @Transactional
    public DepenseDto ajouterDepense(DepenseRequest r, String acteur) {
        Depense d = new Depense();
        d.setDateDepense(r.dateDepense() == null ? LocalDate.now(clock) : r.dateDepense());
        d.setCategorie(r.categorie()); d.setLibelle(r.libelle()); d.setMontant(r.montant()); d.setEnregistrePar(acteur);
        d = depenses.save(d);
        audit.log(acteur, "DEPENSE_ENREGISTREE", "Depense", d.getId(), r.libelle() + " : " + r.montant() + " FCFA");
        return Mapper.depense(d);
    }
}
