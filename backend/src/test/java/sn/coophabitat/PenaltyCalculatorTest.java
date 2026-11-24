package sn.coophabitat;

import org.junit.jupiter.api.Test;
import sn.coophabitat.domain.PlanCotisation;
import sn.coophabitat.service.PenaltyCalculator;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PenaltyCalculatorTest {
    private final PenaltyCalculator calc = new PenaltyCalculator();

    private PlanCotisation plan(String taux, String fixe, int grace) {
        PlanCotisation p = new PlanCotisation();
        p.setMontantMensuel(new BigDecimal("50000"));
        p.setJourEcheance(5);
        p.setTauxPenaliteJournalier(new BigDecimal(taux));
        p.setPenaliteFixe(new BigDecimal(fixe));
        p.setDelaiGraceJours(grace);
        return p;
    }

    @Test void aucunePenaliteAvantEcheance() {
        assertEquals(BigDecimal.ZERO, calc.calculer(new BigDecimal("50000"), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 4), plan("0.1", "0", 0)));
    }
    @Test void aucunePenaliteLeJourDeLEcheance() {
        assertEquals(BigDecimal.ZERO, calc.calculer(new BigDecimal("50000"), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5), plan("0.1", "0", 0)));
    }
    @Test void penaliteProportionnelleAuxJoursDeRetard() {
        // 50 000 x 0,1 % x 10 jours = 500,00
        assertEquals(new BigDecimal("500.00"), calc.calculer(new BigDecimal("50000"), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 15), plan("0.1", "0", 0)));
    }
    @Test void delaiDeGraceEtPenaliteFixe() {
        // 10 jours - 5 de grace = 5 jours : 50 000 x 0,1 % x 5 = 250 + 1 000 fixe
        assertEquals(new BigDecimal("1250.00"), calc.calculer(new BigDecimal("50000"), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 15), plan("0.1", "1000", 5)));
    }
    @Test void montantNulOuNegatifDonneZero() {
        assertEquals(BigDecimal.ZERO, calc.calculer(BigDecimal.ZERO, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1), plan("0.1", "0", 0)));
        assertEquals(BigDecimal.ZERO, calc.calculer(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1), plan("0.1", "0", 0)));
    }
}
