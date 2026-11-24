package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.service.AuditService;
import sn.coophabitat.service.ComptabiliteService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Comptabilite")
public class ComptabiliteController {
    private final ComptabiliteService service;
    private final AuditService audit;
    private final Clock clock;
    public ComptabiliteController(ComptabiliteService service, AuditService audit, Clock clock) {
        this.service = service; this.audit = audit; this.clock = clock;
    }

    @Operation(summary = "Sequestre du fonds commun (visible par tous les membres)")
    @GetMapping("/tresorerie/sequestre")
    public TresorerieDto sequestre() { return service.tresorerie(); }

    @Operation(summary = "Tableau de bord Tresorier : membres a jour / en retard, tresorerie (President / Tresorier)")
    @GetMapping("/dashboard") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public DashboardDto dashboard() { return service.dashboard(); }

    @Operation(summary = "Budget previsionnel d'un exercice (visible par tous)")
    @GetMapping("/budget")
    public List<BudgetLigneDto> budget(@RequestParam(required = false) Integer exercice) {
        return service.budget(exercice == null ? LocalDate.now(clock).getYear() : exercice);
    }

    @Operation(summary = "Ajouter une ligne de budget (Tresorier / President)")
    @PostMapping("/budget") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public BudgetLigneDto ajouterBudget(@Valid @RequestBody BudgetLigneRequest r, Authentication auth) {
        return service.ajouterBudget(r, auth.getName());
    }

    @Operation(summary = "Depenses reelles (visibles par tous)")
    @GetMapping("/depenses")
    public List<DepenseDto> depenses() { return service.depenses(); }

    @Operation(summary = "Enregistrer une depense (Tresorier / President)")
    @PostMapping("/depenses") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public DepenseDto ajouterDepense(@Valid @RequestBody DepenseRequest r, Authentication auth) {
        return service.ajouterDepense(r, auth.getName());
    }

    @Operation(summary = "Journal d'audit (bureau)")
    @GetMapping("/audit") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER','SECRETAIRE')")
    public List<AuditDto> audit() { return audit.recent(); }
}
