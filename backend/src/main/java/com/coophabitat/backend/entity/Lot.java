package com.coophabitat.backend.entity;

import com.coophabitat.backend.enums.StatutLot;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lots")
@Data
public class Lot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    private String secteur;
    private Double superficie;

    @Enumerated(EnumType.STRING)
    private StatutLot statut = StatutLot.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "membre_id")
    private Membre beneficiaire;
}