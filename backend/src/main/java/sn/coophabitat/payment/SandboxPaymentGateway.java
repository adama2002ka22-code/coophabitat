package sn.coophabitat.payment;

import org.springframework.stereotype.Service;
import java.util.UUID;

/** Simulateur sandbox : succes sauf si le numero se termine par 0000 (cas d'echec de demonstration). */
@Service
public class SandboxPaymentGateway implements PaymentGateway {
    @Override
    public ChargeResult charger(ChargeRequest r) {
        String tel = r.telephone() == null ? "" : r.telephone().replaceAll("\\s", "");
        if (tel.length() < 9) return new ChargeResult(false, null, "Numero de telephone invalide");
        if (tel.endsWith("0000")) return new ChargeResult(false, null, "Paiement refuse (sandbox) : solde insuffisant");
        String ref = r.operateur().toUpperCase() + "-SBX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new ChargeResult(true, ref, "Paiement accepte (sandbox)");
    }
}
