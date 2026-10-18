package com.coophabitat.backend.controller;

import com.coophabitat.backend.entity.Paiement;
import com.coophabitat.backend.service.PaiementService;
import com.coophabitat.backend.service.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin(origins = "*")
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    @Autowired
    private PdfReportService pdfReportService;

    // 1. Lister tous les paiements : GET http://localhost:8080/api/paiements
    @GetMapping
    public List<Paiement> getAllPaiements() {
        return paiementService.getAllPaiements();
    }

    // 2. Lister les paiements d'un membre spécifique : GET http://localhost:8080/api/paiements/membre/{id}
    @GetMapping("/membre/{id}")
    public List<Paiement> getPaiementsByMembre(@PathVariable Long id) {
        return paiementService.getPaiementsByMembre(id);
    }

    // 3. Enregistrer un nouveau paiement : POST http://localhost:8080/api/paiements
    @PostMapping
    public Paiement createPaiement(@RequestBody Paiement paiement) {
        return paiementService.savePaiement(paiement);
    }

    // 4. Supprimer un paiement : DELETE http://localhost:8080/api/paiements/{id}
    @DeleteMapping("/{id}")
    public void deletePaiement(@PathVariable Long id) {
        paiementService.deletePaiement(id);
    }

    // 5. Télécharger le reçu de paiement en PDF : GET http://localhost:8080/api/paiements/{id}/recu
    @GetMapping("/{id}/recu")
    public ResponseEntity<byte[]> telechargerRecuPaiement(@PathVariable Long id) {
        try {
            // Récupérer le paiement par son ID
            Paiement paiement = paiementService.getAllPaiements().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec l'id : " + id));

            // Générer le PDF via Jasper Reports
            byte[] pdfBytes = pdfReportService.generatePaiementReceipt(paiement);

            // Configurer les en-têtes HTTP pour forcer le téléchargement du fichier PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "recu_paiement_" + id + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}