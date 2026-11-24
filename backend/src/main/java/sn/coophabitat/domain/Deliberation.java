package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "deliberations")
@Getter @Setter @NoArgsConstructor
public class Deliberation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "assemblee_id") private Assemblee assemblee;
    @Column(nullable = false) private String titre;
    @Column(length = 2000, nullable = false) private String resolution;
    private int pour;
    private int contre;
    private int abstention;
}
