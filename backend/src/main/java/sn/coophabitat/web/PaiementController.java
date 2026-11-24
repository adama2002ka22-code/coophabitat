package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.domain.Paiement;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.security.SecurityUtils;
import sn.coophabitat.service.CotisationService;
import sn.coophabitat.service.MembreService;
import sn.coophabitat.service.PaiementService;
import sn.coophabitat.service.PdfService;
import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@Tag(name = "Paiements")
public class PaiementController {
    private final PaiementService service;
    private final MembreService membres;
    private final CotisationService cotisations;
    private final PdfService pdf;
    public PaiementController(PaiementService service, MembreService membres, CotisationService cotisations, PdfService pdf) {
        this.service = service; this.membres = membres; this.cotisations = cotisations; this.pdf = pdf;
    }

    @Operation(summary = "Saisie manuelle d'un paiement (Tresorier / President)")
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER')")
    public PaiementDto manuel(@Valid @RequestBody PaiementManuelRequest r, Authentication auth) {
        return service.enregistrer(r.membreId(), r.montant(), r.canal(), r.reference(), auth.getName());
    }

    @Operation(summary = "Payer ma cotisation par Mobile Money (Wave / SendFlash - sandbox)")
    @PostMapping("/mobile-money") @ResponseStatus(HttpStatus.CREATED)
    public PaiementDto mobileMoney(@Valid @RequestBody MobileMoneyRequest r, Authentication auth) {
        return service.mobileMoney(membres.getByEmail(auth.getName()), r);
    }

    @Operation(summary = "Derniers paiements de la cooperative (bureau)")
    @GetMapping @PreAuthorize("hasAnyRole('PRESIDENT','TRESORIER','SECRETAIRE')")
    public List<PaiementDto> recents() { return service.recents(); }

    @Operation(summary = "Mes paiements")
    @GetMapping("/me")
    public List<PaiementDto> mes(Authentication auth) { return service.duMembre(membres.getByEmail(auth.getName()).getId()); }

    @Operation(summary = "Recu PDF d'un paiement (proprietaire ou bureau)")
    @GetMapping("/{id}/recu")
    public ResponseEntity<byte[]> recu(@PathVariable Long id, Authentication auth) {
        Paiement p = service.get(id);
        Membre caller = membres.getByEmail(auth.getName());
        if (!SecurityUtils.isBureau(auth) && !p.getMembre().getId().equals(caller.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        byte[] bytes = pdf.recu(p, cotisations.situation(p.getMembre().getId()).soldeRestantDu());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + p.getRecuNumero() + ".pdf\"").body(bytes);
    }
}
