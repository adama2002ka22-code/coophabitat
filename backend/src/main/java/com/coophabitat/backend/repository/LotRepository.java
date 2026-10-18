package com.coophabitat.backend.repository;

import com.coophabitat.backend.entity.Lot;
import com.coophabitat.backend.enums.StatutLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    List<Lot> findByStatut(StatutLot statut);
}