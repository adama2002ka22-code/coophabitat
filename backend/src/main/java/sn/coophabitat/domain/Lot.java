package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "lots")
@Getter @Setter @NoArgsConstructor
public class Lot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String reference;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TypeLot typeLot = TypeLot.TERRAIN;
    @Column(nullable = false) private double superficie;
    private String localisation;
    private Double latitude;
    private Double longitude;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutLot statut = StatutLot.DISPONIBLE;
}
