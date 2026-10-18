package com.coophabitat.backend.repository;

import com.coophabitat.backend.entity.Parcelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelleRepository extends JpaRepository<Parcelle, Long> {

    // Trouver une parcelle par son numéro de lot unique
    Optional<Parcelle> findByNumeroLot(String numeroLot);

    // Trouver toutes les parcelles par leur statut (ex: "DISPONIBLE", "ATTRIBUE")
    List<Parcelle> findByStatut(String statut);
}