package sn.coophabitat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.coophabitat.domain.AuditLog;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByDateActionDesc();
}
