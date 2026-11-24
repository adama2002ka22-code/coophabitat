package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Plan de cotisation configurable. Un seul plan actif a la fois (l'historique est conserve). */
@Entity @Table(name = "plans_cotisation")
@Getter @Setter @NoArgsConstructor
public class PlanCotisation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montantMensuel;
    @Column(nullable = false) private int jourEcheance;
    /** Penalite = montantDu x taux(%) x jours de retard (apres delai de grace) + penalite fixe. */
    @Column(nullable = false, precision = 10, scale = 4) private BigDecimal tauxPenaliteJournalier;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal penaliteFixe;
    @Column(nullable = false) private int delaiGraceJours;
    @Column(nullable = false) private boolean actif = true;
    @Column(nullable = false) private LocalDate dateEffet = LocalDate.now();
}
