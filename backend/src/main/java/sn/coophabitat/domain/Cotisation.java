package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "cotisations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"membre_id", "date_echeance"}))
@Getter @Setter @NoArgsConstructor
public class Cotisation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "membre_id") private Membre membre;
    @Column(nullable = false) private LocalDate dateEcheance;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montantDu;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal penalite = BigDecimal.ZERO;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montantPaye = BigDecimal.ZERO;
    @Column(nullable = false) private boolean rappelEnvoye = false;

    @Transient
    public BigDecimal getSolde() { return montantDu.add(penalite).subtract(montantPaye); }
}
