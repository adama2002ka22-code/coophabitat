package sn.coophabitat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.repository.*;
import sn.coophabitat.service.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** Plan par defaut + (si app.seed.enabled) comptes et donnees de demonstration pour la soutenance. */
@Component
public class DataInitializer implements CommandLineRunner {
    private final boolean seed;
    private final String password;
    private final PlanCotisationService plans;
    private final MembreService membres;
    private final MembreRepository membreRepo;
    private final CotisationService cotisations;
    private final PaiementService paiements;
    private final LotRepository lotRepo;
    private final LotService lotService;
    private final BudgetLigneRepository budgetRepo;
    private final DepenseRepository depenseRepo;
    private final AssembleeRepository assembleeRepo;
    private final AssembleeService assembleeService;
    private final Clock clock;

    public DataInitializer(@Value("${app.seed.enabled}") boolean seed, @Value("${app.seed.password}") String password,
                           PlanCotisationService plans, MembreService membres, MembreRepository membreRepo,
                           CotisationService cotisations, PaiementService paiements, LotRepository lotRepo,
                           LotService lotService, BudgetLigneRepository budgetRepo, DepenseRepository depenseRepo,
                           AssembleeRepository assembleeRepo, AssembleeService assembleeService, Clock clock) {
        this.seed = seed; this.password = password; this.plans = plans; this.membres = membres; this.membreRepo = membreRepo;
        this.cotisations = cotisations; this.paiements = paiements; this.lotRepo = lotRepo; this.lotService = lotService;
        this.budgetRepo = budgetRepo; this.depenseRepo = depenseRepo; this.assembleeRepo = assembleeRepo;
        this.assembleeService = assembleeService; this.clock = clock;
    }

    @Override
    public void run(String... args) {
        plans.actif();
        YearMonth now = YearMonth.now(clock);
        if (seed && membreRepo.count() == 0) seedDemo(now);
        cotisations.genererEcheances(now);
        cotisations.genererEcheances(now.plusMonths(1));
    }

    private Membre creer(String nom, String email, String tel, TypeMembre type, Role role) {
        membres.createAsSystem(new CreateMembreRequest(nom, email, tel, password, type, role));
        Membre m = membreRepo.findByEmailIgnoreCase(email).orElseThrow();
        m.setDateAdhesion(LocalDate.now(clock).minusMonths(4));
        membreRepo.save(m);
        return m;
    }

    private void seedDemo(YearMonth now) {
        Membre pres = creer("Mamadou Diallo", "president@coophabitat.sn", "+221770000001", TypeMembre.FONDATEUR, Role.PRESIDENT);
        Membre tres = creer("Aminata Sow", "tresorier@coophabitat.sn", "+221770000002", TypeMembre.FONDATEUR, Role.TRESORIER);
        Membre secr = creer("Ousmane Ndiaye", "secretaire@coophabitat.sn", "+221770000003", TypeMembre.ORDINAIRE, Role.SECRETAIRE);
        Membre coumbel = creer("Coumbel Ka", "coumbel.ka@coophabitat.sn", "+221778452011", TypeMembre.ORDINAIRE, Role.MEMBRE);
        Membre abdou = creer("Abdoulaye Diallo", "abdoulaye.d@coophabitat.sn", "+221761234567", TypeMembre.BENEFICIAIRE, Role.MEMBRE);
        Membre fatou = creer("Fatou Diop", "fatou.diop@coophabitat.sn", "+221775551122", TypeMembre.ORDINAIRE, Role.MEMBRE);
        Membre cheikh = creer("Cheikh Fall", "cheikh.fall@coophabitat.sn", "+221774449988", TypeMembre.FONDATEUR, Role.MEMBRE);

        for (int k = 3; k >= 1; k--) cotisations.genererEcheances(now.minusMonths(k));
        cotisations.genererEcheances(now);
        cotisations.genererEcheances(now.plusMonths(1));

        BigDecimal mensualite = plans.actif().getMontantMensuel();
        payerExigible(pres, CanalPaiement.VIREMENT);
        payerExigible(tres, CanalPaiement.ESPECES);
        payerExigible(secr, CanalPaiement.WAVE);
        payerExigible(coumbel, CanalPaiement.WAVE);
        payerExigible(fatou, CanalPaiement.SENDFLASH);
        payerExigible(cheikh, CanalPaiement.ESPECES);
        paiements.enregistrer(abdou.getId(), mensualite, CanalPaiement.ESPECES, null, "seed"); // reste en retard

        lotService.create(new LotRequest("Lot A-01", TypeLot.TERRAIN, 300, "Cite Keur Gorgui, Dakar", 14.6937, -17.4441), "seed");
        lotService.create(new LotRequest("Lot A-02", TypeLot.TERRAIN, 250, "Cite Keur Gorgui, Dakar", 14.6945, -17.4420), "seed");
        lotService.create(new LotRequest("Lot B-09", TypeLot.TERRAIN, 400, "Extension Diamniadio", 14.7167, -17.1833), "seed");
        lotService.create(new LotRequest("Villa V-01", TypeLot.LOGEMENT, 120, "Residence Les Almadies", 14.7450, -17.5200), "seed");
        lotRepo.findAll().stream().filter(l -> l.getReference().equals("Lot B-09")).findFirst().ifPresent(l -> {
            l.setStatut(StatutLot.LITIGIEUX);
            lotRepo.save(l);
        });

        int annee = LocalDate.now(clock).getYear();
        addBudget(annee, "VRD", "Voirie et reseaux divers", "15000000");
        addBudget(annee, "Etudes", "Etudes topographiques et geotechniques", "5000000");
        addBudget(annee, "Foncier", "Frais de titre foncier", "10000000");
        addBudget(annee, "Fonctionnement", "Frais de gestion de la cooperative", "5000000");
        addDepense("VRD", "Terrassement phase 1", "4500000", 60);
        addDepense("Etudes", "Etude topographique", "1200000", 45);
        addDepense("Foncier", "Frais de bornage", "900000", 20);

        assembleeService.convoquer(new AssembleeRequest("Assemblee Generale Ordinaire", LocalDateTime.now(clock).plusDays(10),
                "Siege de la cooperative, Dakar", "1. Approbation des comptes\n2. Avancement des travaux VRD\n3. Attribution des lots\n4. Questions diverses"),
                "seed");
    }

    private void payerExigible(Membre m, CanalPaiement canal) {
        BigDecimal du = cotisations.situation(m.getId()).soldeRestantDu();
        if (du.signum() > 0) paiements.enregistrer(m.getId(), du, canal, null, "seed");
    }

    private void addBudget(int annee, String cat, String lib, String montant) {
        BudgetLigne b = new BudgetLigne();
        b.setExercice(annee); b.setCategorie(cat); b.setLibelle(lib); b.setMontantPrevu(new BigDecimal(montant));
        budgetRepo.save(b);
    }

    private void addDepense(String cat, String lib, String montant, int joursAvant) {
        Depense d = new Depense();
        d.setDateDepense(LocalDate.now(clock).minusDays(joursAvant)); d.setCategorie(cat); d.setLibelle(lib);
        d.setMontant(new BigDecimal(montant)); d.setEnregistrePar("seed");
        depenseRepo.save(d);
    }
}
