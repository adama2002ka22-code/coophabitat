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
import sn.coophabitat.domain.Attribution;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.security.SecurityUtils;
import sn.coophabitat.service.AttributionService;
import sn.coophabitat.service.LotService;
import sn.coophabitat.service.MembreService;
import sn.coophabitat.service.PdfService;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Lots et attributions")
public class LotController {
    private final LotService lots;
    private final AttributionService attributions;
    private final MembreService membres;
    private final PdfService pdf;
    public LotController(LotService lots, AttributionService attributions, MembreService membres, PdfService pdf) {
        this.lots = lots; this.attributions = attributions; this.membres = membres; this.pdf = pdf;
    }

    @Operation(summary = "Liste des lots (visible par tous les membres)")
    @GetMapping("/lots")
    public List<LotDto> list() { return lots.list(); }

    @Operation(summary = "Creer un lot (President / Secretaire)")
    @PostMapping("/lots") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public LotDto create(@Valid @RequestBody LotRequest r, Authentication auth) { return lots.create(r, auth.getName()); }

    @Operation(summary = "Marquer un lot disponible / litigieux (President / Secretaire)")
    @PatchMapping("/lots/{id}/statut") @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public LotDto statut(@PathVariable Long id, @Valid @RequestBody LotStatutRequest r, Authentication auth) {
        return lots.changerStatut(id, r.statut(), auth.getName());
    }

    @Operation(summary = "Tirage au sort (President / Secretaire)")
    @PostMapping("/lots/{id}/tirage") @PreAuthorize("hasAnyRole('PRESIDENT','SECRETAIRE')")
    public AttributionDto tirage(@PathVariable Long id, Authentication auth) { return attributions.tirageAuSort(id, auth.getName()); }

    @Operation(summary = "Attribution par decision de commission (President)")
    @PostMapping("/lots/{id}/commission") @PreAuthorize("hasRole('PRESIDENT')")
    public AttributionDto commission(@PathVariable Long id, @Valid @RequestBody CommissionRequest r, Authentication auth) {
        return attributions.commission(id, r, auth.getName());
    }

    @Operation(summary = "Attributions (bureau : toutes ; membre : les siennes)")
    @GetMapping("/attributions")
    public List<AttributionDto> attributions(Authentication auth) {
        return attributions.list(membres.getByEmail(auth.getName()), SecurityUtils.isBureau(auth));
    }

    @Operation(summary = "PV d'attribution signe (PDF)")
    @GetMapping("/attributions/{id}/pv")
    public ResponseEntity<byte[]> pv(@PathVariable Long id, Authentication auth) {
        Attribution a = attributions.get(id);
        Membre caller = membres.getByEmail(auth.getName());
        if (!SecurityUtils.isBureau(auth) && !a.getBeneficiaire().getId().equals(caller.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.getPvNumero() + ".pdf\"")
                .body(pdf.pv(a, attributions.verifier(a)));
    }

    @Operation(summary = "Verifier l'integrite / la signature d'un PV")
    @GetMapping("/attributions/{id}/verification")
    public VerificationDto verification(@PathVariable Long id) {
        Attribution a = attributions.get(id);
        return new VerificationDto(a.getPvNumero(), attributions.verifier(a), a.getPvHash());
    }
}
