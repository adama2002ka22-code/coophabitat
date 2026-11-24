package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.coophabitat.dto.Dtos.RapportFinancier;
import sn.coophabitat.service.ExcelService;
import sn.coophabitat.service.PdfService;
import sn.coophabitat.service.RapportService;

@RestController
@RequestMapping("/api/rapports/financier")
@Tag(name = "Rapports financiers")
public class RapportController {
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final RapportService rapports;
    private final PdfService pdf;
    private final ExcelService excel;
    public RapportController(RapportService rapports, PdfService pdf, ExcelService excel) {
        this.rapports = rapports; this.pdf = pdf; this.excel = excel;
    }

    @Operation(summary = "Rapport financier semestriel (JSON)")
    @GetMapping
    public RapportFinancier json(@RequestParam int annee, @RequestParam int semestre) { return rapports.semestriel(annee, semestre); }

    @Operation(summary = "Export PDF du rapport financier semestriel")
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam int annee, @RequestParam int semestre) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-" + annee + "-S" + semestre + ".pdf\"")
                .body(pdf.rapport(rapports.semestriel(annee, semestre)));
    }

    @Operation(summary = "Export Excel du rapport financier semestriel")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam int annee, @RequestParam int semestre) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(XLSX))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rapport-" + annee + "-S" + semestre + ".xlsx\"")
                .body(excel.rapport(rapports.semestriel(annee, semestre)));
    }
}
