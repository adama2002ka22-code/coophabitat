package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Deliberation;
import java.util.List;

public interface DeliberationRepository extends JpaRepository<Deliberation, Long> {
    List<Deliberation> findByAssembleeIdOrderByIdAsc(Long assembleeId);
}
