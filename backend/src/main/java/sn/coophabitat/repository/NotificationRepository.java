package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop100ByDestinataireIdOrderByDateCreationDesc(Long membreId);
}
