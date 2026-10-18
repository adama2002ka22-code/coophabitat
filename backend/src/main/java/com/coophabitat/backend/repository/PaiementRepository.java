package com.coophabitat.backend.repository;

import com.coophabitat.backend.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    // Trouver tous les paiements effectués par un membre spécifique
    List<Paiement> findByMembreId(Long membreId);
}