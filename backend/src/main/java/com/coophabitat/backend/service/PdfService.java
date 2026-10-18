package com.coophabitat.backend.service;

import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfService {

    public ByteArrayInputStream genererRecuPaiementPdf(
            Long id, Double montant, String date, String type, String statut, String nomMembre, String emailMembre) {
        try {
            ClassPathResource resource = new ClassPathResource("reports/recu_paiement.jrxml");
            if (!resource.exists()) {
                throw new RuntimeException("Fichier introuvable : resources/reports/recu_paiement.jrxml");
            }

            InputStream inputStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("P_ID_PAIEMENT", id);
            parameters.put("P_MONTANT", montant);
            parameters.put("P_DATE", date);
            parameters.put("P_TYPE", type);
            parameters.put("P_STATUT", statut);
            parameters.put("P_NOM_MEMBRE", nomMembre);
            parameters.put("P_EMAIL_MEMBRE", emailMembre);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            return new ByteArrayInputStream(pdfBytes);
        } catch (Exception e) {
            System.err.println("ERREUR CRITIQUE JASPERREPORTS :");
            e.printStackTrace();
            throw new RuntimeException("Erreur interne JasperReports : " + e.getMessage());
        }
    }
}