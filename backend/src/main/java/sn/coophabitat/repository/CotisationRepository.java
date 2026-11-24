package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.coophabitat.domain.Cotisation;
import java.time.LocalDate;
import java.util.List;

public interface CotisationRepository extends JpaRepository<Cotisation, Long> {
    boolean existsByMembreIdAndDateEcheance(Long membreId, LocalDate dateEcheance);
    List<Cotisation> findByMembreIdOrderByDateEcheanceAsc(Long membreId);
    List<Cotisation> findByDateEcheanceAndRappelEnvoyeFalse(LocalDate dateEcheance);

    @Query("select c from Cotisation c where c.dateEcheance < :today and c.montantPaye < c.montantDu")
    List<Cotisation> findPrincipalImpayeEchu(@Param("today") LocalDate today);

    @Query("select c from Cotisation c where c.dateEcheance < :today and c.montantPaye < c.montantDu + c.penalite")
    List<Cotisation> findEnRetard(@Param("today") LocalDate today);

    @Query("select count(c) from Cotisation c where c.membre.id = :id and c.dateEcheance < :today and c.montantPaye < c.montantDu + c.penalite")
    long countEnRetard(@Param("id") Long id, @Param("today") LocalDate today);
}
