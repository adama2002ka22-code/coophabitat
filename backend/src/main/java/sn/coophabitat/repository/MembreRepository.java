package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.domain.StatutMembre;
import java.util.List;
import java.util.Optional;

public interface MembreRepository extends JpaRepository<Membre, Long> {
    Optional<Membre> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Membre> findByStatut(StatutMembre statut);
}
