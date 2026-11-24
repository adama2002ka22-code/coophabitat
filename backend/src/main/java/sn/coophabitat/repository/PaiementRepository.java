package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.coophabitat.domain.Paiement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByMembreIdOrderByDatePaiementDesc(Long membreId);
    List<Paiement> findTop200ByOrderByDatePaiementDesc();
    boolean existsByReference(String reference);

    @Query("select sum(p.montant) from Paiement p")
    BigDecimal totalEncaisse();

    @Query("select sum(p.montant) from Paiement p where p.membre.id = :id")
    BigDecimal totalPourMembre(@Param("id") Long id);

    @Query("select sum(p.montant) from Paiement p where p.datePaiement >= :debut and p.datePaiement < :fin")
    BigDecimal totalEntre(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}
