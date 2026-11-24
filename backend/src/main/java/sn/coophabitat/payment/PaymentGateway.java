package sn.coophabitat.payment;

import java.math.BigDecimal;

/** Abstraction du prestataire Mobile Money (Wave, SendFlash...). Brancher une vraie API = nouvelle implementation. */
public interface PaymentGateway {
    record ChargeRequest(String telephone, BigDecimal montant, String operateur) {}
    record ChargeResult(boolean succes, String reference, String message) {}
    ChargeResult charger(ChargeRequest request);
}
