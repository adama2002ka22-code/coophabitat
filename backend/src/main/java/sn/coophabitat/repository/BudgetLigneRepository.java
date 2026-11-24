package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.BudgetLigne;
import java.util.List;

public interface BudgetLigneRepository extends JpaRepository<BudgetLigne, Long> {
    List<BudgetLigne> findByExerciceOrderByCategorieAsc(int exercice);
}
