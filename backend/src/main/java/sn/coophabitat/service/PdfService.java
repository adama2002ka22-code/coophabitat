package sn.coophabitat.service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sn.coophabitat.domain.Attribution;
import sn.coophabitat.domain.Paiement;
import sn.coophabitat.dto.Dtos.BudgetLigneDto;
import sn.coophabitat.dto.Dtos.RapportFinancier;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Generation des PDF via JasperReports : recu de paiement, PV d'attribution signe PKCS#12, rapport financier semestriel. */
@Service
public class PdfService {
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final Map<String, JasperReport> compiledTemplates = new ConcurrentHashMap<>();
    private final PdfDigitalSigner signer;

    public PdfService(PdfDigitalSigner signer) {
        this.signer = signer;
    }

    public static String fcfa(BigDecimal v) {
        return String.format(Locale.US, "%,.0f", v == null ? BigDecimal.ZERO : v).replace(',', ' ') + " FCFA";
    }

    private JasperReport template(String path) {
        return compiledTemplates.computeIfAbsent(path, p -> {
            try (InputStream in = new ClassPathResource(p).getInputStream()) {
                return JasperCompileManager.compileReport(in);
            } catch (IOException | JRException e) {
                throw new IllegalStateException("Erreur de compilation du modele JasperReports " + p, e);
            }
        });
    }

    private byte[] render(String templatePath, Map<String, Object> params, JRDataSource dataSource) {
        try {
            JasperPrint print = JasperFillManager.fillReport(template(templatePath), params, dataSource);
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            throw new IllegalStateException("Erreur de generation PDF JasperReports", e);
        }
    }

    public byte[] recu(Paiement p, BigDecimal soldeRestant) {
        Map<String, Object> params = new HashMap<>();
        params.put("P_RECU_NUMERO", p.getRecuNumero());
        params.put("P_DATE", p.getDatePaiement().format(DT));
        params.put("P_ADHERENT", p.getMembre().getNom());
        params.put("P_NUMERO_ADHERENT", p.getMembre().getNumeroAdherent());
        params.put("P_MONTANT", fcfa(p.getMontant()));
        params.put("P_CANAL", p.getCanal().name());
        params.put("P_REFERENCE", p.getReference());
        params.put("P_SOLDE_RESTANT", fcfa(soldeRestant));
        return render("reports/recu_paiement.jrxml", params, new JREmptyDataSource());
    }

    public byte[] pv(Attribution a, boolean valide) {
        Map<String, Object> params = new HashMap<>();
        params.put("P_PV_NUMERO", a.getPvNumero());
        params.put("P_DATE", a.getDateAttribution().format(DT));
        params.put("P_LOT", a.getLot().getReference() + " (" + a.getLot().getTypeLot() + ", " + a.getLot().getSuperficie() + " m2)");
        String loc = (a.getLot().getLocalisation() == null ? "" : a.getLot().getLocalisation())
                + (a.getLot().getLatitude() == null ? "" : "  GPS " + a.getLot().getLatitude() + ", " + a.getLot().getLongitude());
        params.put("P_LOCALISATION", loc);
        params.put("P_BENEFICIAIRE", a.getBeneficiaire().getNom() + " (" + a.getBeneficiaire().getNumeroAdherent() + ")");
        params.put("P_MODE", a.getMode() == null ? "" : a.getMode().name());
        params.put("P_EFFECTUE_PAR", a.getEffectuePar());
        params.put("P_DETAILS", a.getDetails() == null ? "" : a.getDetails());
        params.put("P_STATUT_SIGNATURE", valide ? "INTEGRITE VERIFIEE" : "SIGNATURE INVALIDE");
        params.put("P_HASH", a.getPvHash());
        params.put("P_SIGNATURE", a.getPvSignature());
        byte[] unsigned = render("reports/pv_attribution.jrxml", params, new JREmptyDataSource());
        return signer.sign(unsigned, "Validation officielle du PV d'attribution " + a.getPvNumero(), a.getPvNumero());
    }

    public byte[] rapport(RapportFinancier r) {
        Map<String, Object> params = new HashMap<>();
        params.put("P_TITRE_PERIODE", "RAPPORT FINANCIER - SEMESTRE " + r.semestre() + " " + r.annee()
                + " (" + r.debut() + " au " + r.fin() + ")");
        params.put("P_BUDGET_PREVISIONNEL", fcfa(r.budgetPrevisionnel()));
        params.put("P_ENCAISSEMENTS", fcfa(r.encaissements()));
        params.put("P_DEPENSES", fcfa(r.depenses()));
        params.put("P_TRESORERIE", fcfa(r.tresorerieDisponible()));
        params.put("P_MEMBRES_STATUT", r.membresAJour() + " / " + r.membresEnRetard());

        Collection<Map<String, ?>> rows = new ArrayList<>();
        for (BudgetLigneDto l : r.lignesBudget()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("section", "Budget");
            row.put("categorie", l.categorie());
            row.put("libelle", l.libelle());
            row.put("montant", fcfa(l.montantPrevu()));
            rows.add(row);
        }
        for (Map.Entry<String, BigDecimal> e : r.depensesParCategorie().entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("section", "Depense");
            row.put("categorie", e.getKey());
            row.put("libelle", "Depenses reelles cumulees");
            row.put("montant", fcfa(e.getValue()));
            rows.add(row);
        }
        return render("reports/rapport_financier.jrxml", params, new JRMapCollectionDataSource(rows));
    }
}
