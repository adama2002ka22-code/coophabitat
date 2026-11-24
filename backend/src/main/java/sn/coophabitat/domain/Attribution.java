package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "attributions")
@Getter @Setter @NoArgsConstructor
public class Attribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "lot_id", unique = true) private Lot lot;
    @ManyToOne(optional = false) @JoinColumn(name = "beneficiaire_id") private Membre beneficiaire;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ModeAttribution mode;
    @Column(length = 4000) private String details;
    @Column(nullable = false) private LocalDateTime dateAttribution;
    private String effectuePar;
    @Column(unique = true) private String pvNumero;
    private String pvHash;
    private String pvSignature;
}
