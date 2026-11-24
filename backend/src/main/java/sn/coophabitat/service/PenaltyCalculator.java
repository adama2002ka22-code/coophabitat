package sn.coophabitat.service;

import org.springframework.stereotype.Component;
import sn.coophabitat.domain.PlanCotisation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Calcul pur des interets de retard : montantDu x taux(%) x jours de retard (apres grace) + penalite fixe. */
@Component
public class PenaltyCalculator {
    public BigDecimal calculer(BigDecimal montantDu, LocalDate echeance, LocalDate aujourdhui, PlanCotisation plan) {
        if (montantDu == null || montantDu.signum() <= 0) return BigDecimal.ZERO;
        long jours = ChronoUnit.DAYS.between(echeance, aujourdhui) - plan.getDelaiGraceJours();
        if (jours <= 0) return BigDecimal.ZERO;
        BigDecimal variable = montantDu.multiply(plan.getTauxPenaliteJournalier())
                .multiply(BigDecimal.valueOf(jours)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return variable.add(plan.getPenaliteFixe()).setScale(2, RoundingMode.HALF_UP);
    }
}
