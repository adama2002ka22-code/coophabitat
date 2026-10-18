package com.coophabitat.backend.entity;

import com.coophabitat.backend.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "cotisations")
@Data
public class Cotisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;

    private String modePaiement;
    private String referenceQuittance;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut = StatutPaiement.EN_RETARD;

    @ManyToOne
    @JoinColumn(name = "membre_id", nullable = false)
    private Membre membre;
}