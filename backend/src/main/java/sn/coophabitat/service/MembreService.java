package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.MembreRepository;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
public class MembreService {
    private final MembreRepository repository;
    private final PasswordEncoder encoder;
    private final CotisationService cotisationService;
    private final AuditService audit;

    public MembreService(MembreRepository repository, PasswordEncoder encoder, CotisationService cotisationService,
                         AuditService audit) {
        this.repository = repository; this.encoder = encoder; this.cotisationService = cotisationService; this.audit = audit;
    }

    /** Inscription publique : le role est toujours MEMBRE (pas d'escalade de privileges). */
    @Transactional
    public MembreResponse register(RegisterRequest req) {
        TypeMembre type = req.typeMembre() == null ? TypeMembre.ORDINAIRE : req.typeMembre();
        Membre m = persist(req.nom(), req.email(), req.telephone(), req.motDePasse(), type, Role.MEMBRE);
        audit.log(m.getEmail(), "MEMBRE_INSCRIT", "Membre", m.getId(), "auto-inscription");
        return toResponse(m);
    }

    /** Creation par le bureau : seul le President attribue un role autre que MEMBRE. */
    @Transactional
    public MembreResponse create(CreateMembreRequest req, String callerEmail) {
        Membre caller = getByEmail(callerEmail);
        if (req.role() != Role.MEMBRE && caller.getRole() != Role.PRESIDENT)
            throw new AccessDeniedException("Seul le President peut attribuer un role du bureau");
        Membre m = persist(req.nom(), req.email(), req.telephone(), req.motDePasse(), req.typeMembre(), req.role());
        audit.log(callerEmail, "MEMBRE_CREE", "Membre", m.getId(), "role=" + req.role() + ", type=" + req.typeMembre());
        return toResponse(m);
    }

    @Transactional
    public MembreResponse createAsSystem(CreateMembreRequest req) {
        return toResponse(persist(req.nom(), req.email(), req.telephone(), req.motDePasse(), req.typeMembre(), req.role()));
    }

    @Transactional(readOnly = true)
    public List<MembreResponse> findAll() {
        Set<Long> retards = cotisationService.membresEnRetardIds();
        return repository.findAll().stream().map(m -> Mapper.membre(m, retards.contains(m.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public MembreResponse me(String email) { return toResponse(getByEmail(email)); }

    @Transactional(readOnly = true)
    public MembreResponse toResponse(Membre m) { return Mapper.membre(m, cotisationService.estEnRetard(m.getId())); }

    @Transactional(readOnly = true)
    public Membre getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
    }

    @Transactional
    public MembreResponse updateStatut(Long id, StatutMembre statut, String acteur) {
        Membre m = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
        m.setStatut(statut);
        audit.log(acteur, "MEMBRE_STATUT", "Membre", id, "statut=" + statut);
        return toResponse(m);
    }

    private Membre persist(String nom, String email, String tel, String rawPassword, TypeMembre type, Role role) {
        if (repository.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est deja utilise");
        Membre m = new Membre();
        m.setNom(nom.trim());
        m.setEmail(email.trim().toLowerCase());
        m.setTelephone(tel);
        m.setMotDePasse(encoder.encode(rawPassword));
        m.setTypeMembre(type);
        m.setRole(role);
        m = repository.save(m);
        m.setNumeroAdherent(String.format("ADH-%d-%03d", Year.now().getValue(), m.getId()));
        cotisationService.genererPour(m);
        return m;
    }
}
