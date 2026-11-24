package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "assemblees")
@Getter @Setter @NoArgsConstructor
public class Assemblee {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String titre;
    @Column(nullable = false) private LocalDateTime dateAssemblee;
    private String lieu;
    @Column(length = 4000, nullable = false) private String ordreDuJour;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutAssemblee statut = StatutAssemblee.CONVOQUEE;
    @Column(length = 8000) private String compteRendu;
    @Column(nullable = false) private boolean compteRenduPublie = false;
}
