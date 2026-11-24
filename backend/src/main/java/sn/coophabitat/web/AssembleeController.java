package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.service.AssembleeService;
import java.util.List;

@RestController
@RequestMapping("/api/assemblees")
@Tag(name = "Assemblee generale")
public class AssembleeController {
    private final AssembleeService service;
    public AssembleeController(AssembleeService service) { this.service = service; }

    @Operation(summary = "Liste des assemblees (convocations, ordre du jour, deliberations)")
    @GetMapping
    public List<AssembleeDto> list() { return service.list(); }

    @Operation(summary = "Detail d'une assemblee")
    @GetMapping("/{id}")
    public AssembleeDto get(@PathVariable Long id) { return service.get(id); }

    @Operation(summary = "Convoquer une AG - notifie tous les membres (President / Secretaire)")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public AssembleeDto convoquer(@Valid @RequestBody AssembleeRequest r, Authentication auth) { return service.convoquer(r, auth.getName()); }

    @Operation(summary = "Enregistrer une deliberation (President / Secretaire)")
    @PostMapping("/{id}/deliberations") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public AssembleeDto deliberer(@PathVariable Long id, @Valid @RequestBody DeliberationRequest r, Authentication auth) {
        return service.deliberer(id, r, auth.getName());
    }

    @Operation(summary = "Rediger / publier le compte rendu (President / Secretaire)")
    @PutMapping("/{id}/compte-rendu") @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public AssembleeDto compteRendu(@PathVariable Long id, @Valid @RequestBody CompteRenduRequest r, Authentication auth) {
        return service.compteRendu(id, r, auth.getName());
    }
}
