package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.Assemblee;
import sn.coophabitat.domain.Deliberation;
import sn.coophabitat.domain.StatutAssemblee;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.AssembleeRepository;
import sn.coophabitat.repository.DeliberationRepository;
import java.util.List;

@Service
public class AssembleeService {
    private final AssembleeRepository assemblees;
    private final DeliberationRepository deliberations;
    private final NotificationService notifications;
    private final AuditService audit;

    public AssembleeService(AssembleeRepository assemblees, DeliberationRepository deliberations,
                            NotificationService notifications, AuditService audit) {
        this.assemblees = assemblees; this.deliberations = deliberations; this.notifications = notifications; this.audit = audit;
    }

    private Assemblee find(Long id) {
        return assemblees.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assemblee introuvable"));
    }
    private AssembleeDto dto(Assemblee a) { return Mapper.assemblee(a, deliberations.findByAssembleeIdOrderByIdAsc(a.getId())); }

    @Transactional(readOnly = true)
    public List<AssembleeDto> list() { return assemblees.findAllByOrderByDateAssembleeDesc().stream().map(this::dto).toList(); }

    @Transactional(readOnly = true)
    public AssembleeDto get(Long id) { return dto(find(id)); }

    @Transactional
    public AssembleeDto convoquer(AssembleeRequest r, String acteur) {
        Assemblee a = new Assemblee();
        a.setTitre(r.titre()); a.setDateAssemblee(r.dateAssemblee()); a.setLieu(r.lieu()); a.setOrdreDuJour(r.ordreDuJour());
        a = assemblees.save(a);
        audit.log(acteur, "AG_CONVOQUEE", "Assemblee", a.getId(), a.getTitre());
        notifications.notifierTous("Convocation : " + a.getTitre(),
                "Vous etes convoque(e) le " + a.getDateAssemblee() + (a.getLieu() == null ? "" : " a " + a.getLieu())
                        + ". Ordre du jour : " + a.getOrdreDuJour());
        return dto(a);
    }

    @Transactional
    public AssembleeDto deliberer(Long id, DeliberationRequest r, String acteur) {
        Assemblee a = find(id);
        Deliberation d = new Deliberation();
        d.setAssemblee(a); d.setTitre(r.titre()); d.setResolution(r.resolution());
        d.setPour(r.pour()); d.setContre(r.contre()); d.setAbstention(r.abstention());
        deliberations.save(d);
        audit.log(acteur, "AG_DELIBERATION", "Assemblee", id, r.titre() + " (" + r.pour() + "/" + r.contre() + "/" + r.abstention() + ")");
        return dto(a);
    }

    @Transactional
    public AssembleeDto compteRendu(Long id, CompteRenduRequest r, String acteur) {
        Assemblee a = find(id);
        a.setCompteRendu(r.compteRendu());
        if (r.publier()) {
            a.setCompteRenduPublie(true);
            a.setStatut(StatutAssemblee.TENUE);
            notifications.notifierTous("Compte rendu publie : " + a.getTitre(), "Le compte rendu de l'assemblee est disponible dans l'espace Assemblee.");
        }
        audit.log(acteur, r.publier() ? "AG_COMPTE_RENDU_PUBLIE" : "AG_COMPTE_RENDU_ENREGISTRE", "Assemblee", id, a.getTitre());
        return dto(a);
    }
}
