package com.coophabitat.backend.entity;

import com.coophabitat.backend.enums.RoleCoop;
import com.coophabitat.backend.enums.TypeMembre;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "membres")
@Data
public class Membre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String telephone;

    @Enumerated(EnumType.STRING)
    private TypeMembre typeMembre;

    private boolean actif = true;

    @Enumerated(EnumType.STRING)
    private RoleCoop role;
}