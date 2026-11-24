package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Lot;

public interface LotRepository extends JpaRepository<Lot, Long> {
    boolean existsByReferenceIgnoreCase(String reference);
}
