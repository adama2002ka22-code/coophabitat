package sn.coophabitat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.coophabitat.domain.AuditLog;
import sn.coophabitat.dto.Dtos.AuditDto;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.AuditLogRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    private final Clock clock;
    public AuditService(AuditLogRepository repo, Clock clock) { this.repo = repo; this.clock = clock; }

    @Transactional
    public void log(String acteur, String action, String entite, Long entiteId, String details) {
        AuditLog a = new AuditLog();
        a.setDateAction(LocalDateTime.now(clock));
        a.setActeur(acteur);
        a.setAction(action);
        a.setEntite(entite);
        a.setEntiteId(entiteId);
        a.setDetails(details);
        repo.save(a);
    }

    @Transactional(readOnly = true)
    public List<AuditDto> recent() { return repo.findTop200ByOrderByDateActionDesc().stream().map(Mapper::audit).toList(); }
}
