package com.coophabitat.backend.service;

import com.coophabitat.backend.entity.Paiement;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfReportService {

    // Méthode pour générer un reçu de paiement en PDF
    public byte[] generatePaiementReceipt(Paiement paiement) throws Exception {
        // 1. Charger le fichier modèle Jasper (.jrxml ou .jasper) depuis les ressources
        // (Nous créerons le fichier modèle juste après)
        InputStream reportStream = new ClassPathResource("reports/recu_paiement.jrxml").getInputStream();

        // 2. Compiler le rapport Jasper
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 3. Remplir les paramètres du reçu
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_ID_PAIEMENT", paiement.getId());
        parameters.put("P_MONTANT", paiement.getMontant());
        parameters.put("P_DATE", paiement.getDatePaiement() != null ? paiement.getDatePaiement().toString() : "");
        parameters.put("P_TYPE", paiement.getTypePaiement());
        parameters.put("P_STATUT", paiement.getStatut());

        if (paiement.getMembre() != null) {
            parameters.put("P_NOM_MEMBRE", paiement.getMembre().getPrenom() + " " + paiement.getMembre().getNom());
            parameters.put("P_EMAIL_MEMBRE", paiement.getMembre().getEmail());
        } else {
            parameters.put("P_NOM_MEMBRE", "Inconnu");
            parameters.put("P_EMAIL_MEMBRE", "");
        }

        // 4. Remplir le rapport avec une source vide (puisque tout passe par les paramètres)
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

        // 5. Exporter au format PDF sous forme de tableau de bytes
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}