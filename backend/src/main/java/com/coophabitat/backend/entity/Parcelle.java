package com.coophabitat.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parcelles")
public class Parcelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numéro ou référence du lot (ex: "Lot 04", "Parcelle B12")
    @Column(unique = true, nullable = false)
    private String numeroLot;

    // Superficie en mètres carrés
    private Double superficie;

    // Statut de la parcelle (ex: "DISPONIBLE", "RESERVE", "ATTRIBUE")
    private String statut;

    // Localisation ou zone dans le site de la coopérative
    private String zone;

    // Relation : Une parcelle peut être attribuée à un membre (User)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User proprietaire;

    // Constructeurs
    public Parcelle() {
    }

    public Parcelle(String numeroLot, Double superficie, String statut, String zone, User proprietaire) {
        this.numeroLot = numeroLot;
        this.superficie = superficie;
        this.statut = statut;
        this.zone = zone;
        this.proprietaire = proprietaire;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public void setNumeroLot(String numeroLot) {
        this.numeroLot = numeroLot;
    }

    public Double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Double superficie) {
        this.superficie = superficie;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public User getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(User proprietaire) {
        this.proprietaire = proprietaire;
    }
}