package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.service.CotisationService;
import sn.coophabitat.service.MembreService;
import java.util.List;

@RestController
@RequestMapping("/api/membres")
@Tag(name = "Membres")
public class MembreController {
    private final MembreService service;
    private final CotisationService cotisations;
    public MembreController(MembreService service, CotisationService cotisations) { this.service = service; this.cotisations = cotisations; }

    @Operation(summary = "Liste des membres (bureau)")
    @GetMapping @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER','SECRETAIRE')")
    public List<MembreResponse> list() { return service.findAll(); }

    @Operation(summary = "Profil du membre connecte")
    @GetMapping("/me")
    public MembreResponse me(Authentication auth) { return service.me(auth.getName()); }

    @Operation(summary = "Inscrire un membre (President / Secretaire)")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public MembreResponse create(@Valid @RequestBody CreateMembreRequest req, Authentication auth) { return service.create(req, auth.getName()); }

    @Operation(summary = "Activer / suspendre un membre (President / Secretaire)")
    @PatchMapping("/{id}/statut") @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public MembreResponse statut(@PathVariable Long id, @Valid @RequestBody StatutRequest req, Authentication auth) {
        return service.updateStatut(id, req.statut(), auth.getName());
    }

    @Operation(summary = "Situation de cotisation d'un membre (bureau)")
    @GetMapping("/{id}/situation") @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER','SECRETAIRE')")
    public Situation situation(@PathVariable Long id) { return cotisations.situation(id); }
}
