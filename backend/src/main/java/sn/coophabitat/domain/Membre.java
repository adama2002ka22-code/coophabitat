package sn.coophabitat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity @Table(name = "membres")
@Getter @Setter @NoArgsConstructor
public class Membre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String numeroAdherent;
    @Column(nullable = false) private String nom;
    @Column(nullable = false, unique = true) private String email;
    private String telephone;
    @Column(nullable = false) private String motDePasse;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TypeMembre typeMembre = TypeMembre.ORDINAIRE;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutMembre statut = StatutMembre.ACTIF;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role = Role.MEMBRE;
    @Column(nullable = false) private LocalDate dateAdhesion = LocalDate.now();
}
