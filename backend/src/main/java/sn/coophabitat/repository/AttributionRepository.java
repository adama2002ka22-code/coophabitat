package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Attribution;
import java.util.List;

public interface AttributionRepository extends JpaRepository<Attribution, Long> {
    boolean existsByBeneficiaireId(Long membreId);
    List<Attribution> findAllByOrderByDateAttributionDesc();
    List<Attribution> findByBeneficiaireIdOrderByDateAttributionDesc(Long membreId);
}
