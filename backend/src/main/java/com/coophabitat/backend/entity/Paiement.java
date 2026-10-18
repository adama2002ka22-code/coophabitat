package com.coophabitat.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Montant de la cotisation ou du versement
    private Double montant;

    // Date à laquelle le paiement a été effectué
    private LocalDate datePaiement;

    // Type de paiement (ex: "Cotisation mensuelle", "Achat terrain", "Frais de dossier")
    private String typePaiement;

    // Statut (ex: "EN_ATTENTE", "VALIDE", "REJETE")
    private String statut;

    // Relation : Un paiement est effectué par un utilisateur (Membre)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User membre;

    // Constructeurs
    public Paiement() {
    }

    public Paiement(Double montant, LocalDate datePaiement, String typePaiement, String statut, User membre) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.typePaiement = typePaiement;
        this.statut = statut;
        this.membre = membre;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public User getMembre() {
        return membre;
    }

    public void setMembre(User membre) {
        this.membre = membre;
    }
}