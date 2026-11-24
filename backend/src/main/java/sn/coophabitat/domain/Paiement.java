package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "paiements")
@Getter @Setter @NoArgsConstructor
public class Paiement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "membre_id") private Membre membre;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal montant;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CanalPaiement canal;
    @Column(nullable = false, unique = true) private String reference;
    @Column(unique = true) private String recuNumero;
    @Column(nullable = false) private LocalDateTime datePaiement;
    private String enregistrePar;
}
