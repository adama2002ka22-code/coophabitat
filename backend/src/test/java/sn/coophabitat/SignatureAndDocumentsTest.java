package sn.coophabitat;

import org.junit.jupiter.api.Test;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.BudgetLigneDto;
import sn.coophabitat.dto.Dtos.RapportFinancier;
import sn.coophabitat.service.ExcelService;
import sn.coophabitat.service.PdfDigitalSigner;
import sn.coophabitat.service.PdfService;
import sn.coophabitat.service.SignatureService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SignatureAndDocumentsTest {
    private final PdfDigitalSigner signer = new PdfDigitalSigner(
            "classpath:keystore/coophabitat-pv.p12", "coophabitat-pv-2026", "coophabitat-pv");
    private final PdfService pdf = new PdfService(signer);
    private final ExcelService excel = new ExcelService();

    private Membre membre() {
        Membre m = new Membre();
        m.setId(1L); m.setNom("Coumbel Ka"); m.setNumeroAdherent("ADH-2026-001"); m.setEmail("c@k.sn");
        return m;
    }

    @Test void signatureDeterministeEtSensibleAuContenu() {
        SignatureService s = new SignatureService("secret-pv-secret-pv-secret-pv-1234");
        assertEquals(s.hmac("abc"), s.hmac("abc"));
        assertNotEquals(s.hmac("abc"), s.hmac("abd"));
        assertEquals(64, s.sha256("abc").length());
        assertNotEquals(s.hmac("abc"), new SignatureService("autre-secret-autre-secret-123456789").hmac("abc"));
    }

    @Test void recuPdfGenereParJasperReports() {
        Paiement p = new Paiement();
        p.setMembre(membre()); p.setMontant(new BigDecimal("50000")); p.setCanal(CanalPaiement.WAVE);
        p.setReference("WAVE-1"); p.setRecuNumero("REC-2026-000001"); p.setDatePaiement(LocalDateTime.now());
        byte[] b = pdf.recu(p, BigDecimal.ZERO);
        assertTrue(new String(b, 0, 5).startsWith("%PDF"));
        assertTrue(b.length > 1000);
    }

    @Test void pvPdfGenereParJasperEtSigneNumeriquementPkcs12() {
        Lot lot = new Lot();
        lot.setReference("Lot A-01"); lot.setTypeLot(TypeLot.TERRAIN); lot.setSuperficie(300);
        lot.setLocalisation("Dakar"); lot.setLatitude(14.69); lot.setLongitude(-17.44);
        Attribution a = new Attribution();
        a.setLot(lot); a.setBeneficiaire(membre()); a.setMode(ModeAttribution.TIRAGE_AU_SORT);
        a.setDateAttribution(LocalDateTime.now()); a.setDetails("Tirage auditable"); a.setPvNumero("PV-2026-0001");
        a.setPvHash("abc123"); a.setPvSignature("sig123"); a.setEffectuePar("president@coophabitat.sn");

        byte[] signedPv = pdf.pv(a, true);
        assertTrue(new String(signedPv, 0, 5).startsWith("%PDF"));
        assertTrue(signer.verify(signedPv), "Le PV PDF doit contenir une signature cryptographique PKCS#12 valide");

        byte[] tampered = Arrays.copyOf(signedPv, signedPv.length);
        tampered[tampered.length / 2] ^= 0xFF;
        assertFalse(signer.verify(tampered), "Un PV PDF altere doit echouer a la verification");
    }

    private RapportFinancier rapport() {
        return new RapportFinancier(2026, 1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), new BigDecimal("35000000"),
                List.of(new BudgetLigneDto(1L, 2026, "VRD", "Voirie", new BigDecimal("15000000"))),
                new BigDecimal("1000000"), new BigDecimal("400000"), Map.of("VRD", new BigDecimal("400000")),
                new BigDecimal("600000"), 5, 1);
    }

    @Test void rapportPdfJasperEtExcel() {
        byte[] pdfBytes = pdf.rapport(rapport());
        assertTrue(new String(pdfBytes, 0, 5).startsWith("%PDF"));
        byte[] x = excel.rapport(rapport());
        assertEquals('P', x[0]); // un .xlsx est une archive ZIP : "PK"
        assertEquals('K', x[1]);
    }

    @Test void formatMonetaire() {
        assertEquals("1 234 567 FCFA", PdfService.fcfa(new BigDecimal("1234567")));
    }
}
