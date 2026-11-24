package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.PlanCotisation;
import java.util.List;
import java.util.Optional;

public interface PlanCotisationRepository extends JpaRepository<PlanCotisation, Long> {
    Optional<PlanCotisation> findFirstByActifTrueOrderByIdDesc();
    List<PlanCotisation> findByActifTrue();
}
