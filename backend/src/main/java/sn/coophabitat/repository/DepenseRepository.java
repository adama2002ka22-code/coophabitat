package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sn.coophabitat.domain.Depense;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {
    List<Depense> findAllByOrderByDateDepenseDesc();
    List<Depense> findByDateDepenseBetween(LocalDate debut, LocalDate fin);

    @Query("select sum(d.montant) from Depense d")
    BigDecimal totalDepenses();
}
