package com.coophabitat.backend.repository;

import com.coophabitat.backend.entity.Cotisation;
import com.coophabitat.backend.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, Long> {
    List<Cotisation> findByStatut(StatutPaiement statut);
    List<Cotisation> findByMembreId(Long membreId);
}