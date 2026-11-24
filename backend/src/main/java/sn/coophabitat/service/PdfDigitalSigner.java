package sn.coophabitat.service;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

/** Signature numerique cryptographique PDF (PKCS#12 / certificat X.509 RSA-SHA256) des PV d'attribution. */
@Service
public class PdfDigitalSigner {
    private final PrivateKey privateKey;
    private final Certificate[] certificateChain;

    public PdfDigitalSigner(
            @Value("${app.pv.keystore-path:classpath:keystore/coophabitat-pv.p12}") String keystorePath,
            @Value("${app.pv.keystore-password:coophabitat-pv-2026}") String keystorePassword,
            @Value("${app.pv.keystore-alias:coophabitat-pv}") String alias) {
        try {
            Resource resource = new DefaultResourceLoader().getResource(keystorePath);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] pwd = keystorePassword.toCharArray();
            try (InputStream in = resource.getInputStream()) {
                ks.load(in, pwd);
            }
            String effectiveAlias = (alias != null && ks.containsAlias(alias))
                    ? alias
                    : ks.aliases().nextElement();
            this.privateKey = (PrivateKey) ks.getKey(effectiveAlias, pwd);
            this.certificateChain = ks.getCertificateChain(effectiveAlias);
            if (this.privateKey == null || this.certificateChain == null || this.certificateChain.length == 0) {
                throw new IllegalStateException("Certificat PKCS#12 introuvable pour l'alias " + effectiveAlias);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger le certificat PKCS#12 de signature PV", e);
        }
    }

    /** Signe un PDF avec le certificat X.509 PKCS#12 et appose un tampon de signature visible. */
    public byte[] sign(byte[] unsignedPdf, String reason, String pvNumero) {
        try (PdfReader reader = new PdfReader(unsignedPdf);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfStamper stamper = PdfStamper.createSignature(reader, out, '\0');
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            appearance.setReason(reason);
            appearance.setLocation("Dakar, Senegal - CoopHabitat");
            appearance.setVisibleSignature(new Rectangle(40, 60, 290, 125), 1, "Sig_" + pvNumero);
            appearance.setCrypto(privateKey, certificateChain, null, PdfSignatureAppearance.SELF_SIGNED);
            stamper.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la signature numerique du PV PDF", e);
        }
    }

    /** Verifie qu'un PDF contient une signature cryptographique intacte. */
    public boolean verify(byte[] signedPdf) {
        try (PdfReader reader = new PdfReader(signedPdf)) {
            AcroFields fields = reader.getAcroFields();
            List<String> names = fields.getSignedFieldNames();
            if (names == null || names.isEmpty()) return false;
            for (String name : names) {
                PdfPKCS7 pkcs7 = fields.verifySignature(name);
                if (pkcs7 == null || !pkcs7.verify()) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
