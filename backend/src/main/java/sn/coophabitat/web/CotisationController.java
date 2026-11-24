package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.service.CotisationService;
import sn.coophabitat.service.MembreService;
import sn.coophabitat.service.PlanCotisationService;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Cotisations")
public class CotisationController {
    private final CotisationService cotisations;
    private final PlanCotisationService plans;
    private final MembreService membres;
    private final Clock clock;
    public CotisationController(CotisationService cotisations, PlanCotisationService plans, MembreService membres, Clock clock) {
        this.cotisations = cotisations; this.plans = plans; this.membres = membres; this.clock = clock;
    }

    @Operation(summary = "Plan de cotisation actif")
    @GetMapping("/plan-cotisation")
    public PlanResponse plan() { return Mapper.plan(plans.actif()); }

    @Operation(summary = "Definir un nouveau plan (montant, echeance, penalites) - Tresorier / President")
    @PutMapping("/plan-cotisation") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public PlanResponse definir(@Valid @RequestBody PlanRequest req, Authentication auth) {
        return Mapper.plan(plans.definir(req, auth.getName()));
    }

    @Operation(summary = "Ma situation : solde cumule, calendrier des paiements, historique")
    @GetMapping("/cotisations/me")
    public Situation maSituation(Authentication auth) {
        Membre m = membres.getByEmail(auth.getName());
        return cotisations.situation(m.getId());
    }

    @Operation(summary = "Generer les echeances du mois courant et suivant (Tresorier / President)")
    @PostMapping("/cotisations/generer") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public Map<String, Integer> generer() {
        YearMonth now = YearMonth.now(clock);
        return Map.of("generees", cotisations.genererEcheances(now) + cotisations.genererEcheances(now.plusMonths(1)));
    }

    @Operation(summary = "Declencher manuellement les rappels J-3 (Tresorier / President)")
    @PostMapping("/cotisations/rappels") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public Map<String, Integer> rappels() { return Map.of("rappelsEnvoyes", cotisations.envoyerRappels()); }
}
