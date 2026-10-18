package com.coophabitat.backend.controller;

import com.coophabitat.backend.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @GetMapping("/recu-paiement")
    public ResponseEntity<InputStreamResource> telechargerRecu(
            @RequestParam("id") Long id,
            @RequestParam("montant") Double montant,
            @RequestParam("date") String date,
            @RequestParam("type") String type,
            @RequestParam("statut") String statut,
            @RequestParam("nomMembre") String nomMembre,
            @RequestParam("emailMembre") String emailMembre) {
        try {
            ByteArrayInputStream bis = pdfService.genererRecuPaiementPdf(id, montant, date, type, statut, nomMembre, emailMembre);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=recu_paiement_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}