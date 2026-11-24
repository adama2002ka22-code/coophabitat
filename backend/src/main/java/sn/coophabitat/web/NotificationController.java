package sn.coophabitat.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.dto.Dtos.DocumentDto;
import sn.coophabitat.dto.Dtos.NotificationDto;
import sn.coophabitat.security.SecurityUtils;
import sn.coophabitat.service.DocumentService;
import sn.coophabitat.service.MembreService;
import sn.coophabitat.service.NotificationService;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Notifications et documents")
public class NotificationController {
    private final NotificationService notifications;
    private final DocumentService documents;
    private final MembreService membres;
    public NotificationController(NotificationService notifications, DocumentService documents, MembreService membres) {
        this.notifications = notifications; this.documents = documents; this.membres = membres;
    }

    @Operation(summary = "Mes notifications")
    @GetMapping("/notifications/me")
    public List<NotificationDto> mes(Authentication auth) { return notifications.mesNotifications(membres.getByEmail(auth.getName()).getId()); }

    @Operation(summary = "Marquer une notification comme lue")
    @PostMapping("/notifications/{id}/lu")
    public void lu(@PathVariable Long id, Authentication auth) { notifications.marquerLu(id, membres.getByEmail(auth.getName()).getId()); }

    @Operation(summary = "Documents officiels (PV, comptes rendus, rapports)")
    @GetMapping("/documents")
    public List<DocumentDto> documents(Authentication auth) {
        Membre m = membres.getByEmail(auth.getName());
        return documents.list(m, SecurityUtils.isBureau(auth));
    }
}
