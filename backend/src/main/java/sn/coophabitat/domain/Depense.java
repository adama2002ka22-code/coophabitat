package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "depenses")
@Getter @Setter @NoArgsConstructor
public class Depense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private LocalDate dateDepense;
    @Column(nullable = false) private String categorie;
    @Column(nullable = false) private String libelle;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montant;
    private String enregistrePar;
}
