package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name = "budget_lignes")
@Getter @Setter @NoArgsConstructor
public class BudgetLigne {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private int exercice;
    @Column(nullable = false) private String categorie;
    @Column(nullable = false) private String libelle;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montantPrevu;
}
