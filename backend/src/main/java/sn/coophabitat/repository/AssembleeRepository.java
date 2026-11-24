package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Assemblee;
import java.util.List;

public interface AssembleeRepository extends JpaRepository<Assemblee, Long> {
    List<Assemblee> findAllByOrderByDateAssembleeDesc();
}
