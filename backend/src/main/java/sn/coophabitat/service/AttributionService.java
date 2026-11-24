package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.AttributionRepository;
import sn.coophabitat.repository.LotRepository;
import sn.coophabitat.repository.MembreRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Workflow d'attribution auditable : tirage au sort (SecureRandom) ou decision de commission, PV signe, notifications. */
@Service
public class AttributionService {
    private final LotRepository lots;
    private final AttributionRepository attributions;
    private final MembreRepository membres;
    private final CotisationService cotisationService;
    private final SignatureService signatures;
    private final NotificationService notifications;
    private final AuditService audit;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AttributionService(LotRepository lots, AttributionRepository attributions, MembreRepository membres,
                              CotisationService cotisationService, SignatureService signatures,
                              NotificationService notifications, AuditService audit, Clock clock) {
        this.lots = lots; this.attributions = attributions; this.membres = membres;
        this.cotisationService = cotisationService; this.signatures = signatures;
        this.notifications = notifications; this.audit = audit; this.clock = clock;
    }

    private Lot lotDisponible(Long id) {
        Lot lot = lots.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot introuvable"));
        if (lot.getStatut() != StatutLot.DISPONIBLE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce lot n'est pas disponible (statut " + lot.getStatut() + ")");
        return lot;
    }

    @Transactional
    public AttributionDto tirageAuSort(Long lotId, String acteur) {
        Lot lot = lotDisponible(lotId);
        Set<Long> retards = cotisationService.membresEnRetardIds();
        List<Membre> eligibles = membres.findByStatut(StatutMembre.ACTIF).stream()
                .filter(m -> !retards.contains(m.getId()))
                .filter(m -> !attributions.existsByBeneficiaireId(m.getId()))
                .sorted(Comparator.comparing(Membre::getId)).toList();
        if (eligibles.isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun membre eligible (actif, a jour, sans lot)");
        int rang = random.nextInt(eligibles.size());
        String liste = eligibles.stream().map(Membre::getNumeroAdherent).collect(Collectors.joining(", "));
        String details = "Tirage au sort parmi " + eligibles.size() + " membres eligibles (actifs, a jour, sans lot) : "
                + liste + ". Rang tire : " + (rang + 1) + ".";
        return finaliser(lot, eligibles.get(rang), ModeAttribution.TIRAGE_AU_SORT, details, acteur);
    }

    @Transactional
    public AttributionDto commission(Long lotId, CommissionRequest r, String acteur) {
        Lot lot = lotDisponible(lotId);
        Membre b = membres.findById(r.membreId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
        if (b.getStatut() != StatutMembre.ACTIF) throw new ResponseStatusException(HttpStatus.CONFLICT, "Le beneficiaire doit etre actif");
        if (attributions.existsByBeneficiaireId(b.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce membre a deja un lot");
        String details = "Decision de commission. Membres : " + r.membresCommission() + ". Justification : " + r.justification();
        return finaliser(lot, b, ModeAttribution.COMMISSION, details, acteur);
    }

    private AttributionDto finaliser(Lot lot, Membre b, ModeAttribution mode, String details, String acteur) {
        lot.setStatut(StatutLot.ATTRIBUE);
        Attribution a = new Attribution();
        a.setLot(lot);
        a.setBeneficiaire(b);
        a.setMode(mode);
        a.setDetails(details);
        a.setDateAttribution(LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS));
        a.setEffectuePar(acteur);
        a = attributions.save(a);
        a.setPvNumero(String.format("PV-%d-%04d", Year.now().getValue(), a.getId()));
        String canon = canonical(a);
        a.setPvHash(signatures.sha256(canon));
        a.setPvSignature(signatures.hmac(canon));
        audit.log(acteur, "LOT_ATTRIBUE", "Attribution", a.getId(), lot.getReference() + " -> " + b.getNumeroAdherent() + " (" + mode + ")");
        notifications.notifier(b, "Attribution de lot",
                "Felicitations, le lot " + lot.getReference() + " vous a ete attribue (" + mode + "). PV : " + a.getPvNumero() + ".");
        notifications.notifierTous("Resultat d'attribution",
                "Le lot " + lot.getReference() + " a ete attribue a " + b.getNom() + " (" + mode + "). PV : " + a.getPvNumero() + ".");
        return Mapper.attribution(a);
    }

    public String canonical(Attribution a) {
        return String.join("|", a.getPvNumero(), a.getLot().getReference(), a.getBeneficiaire().getNumeroAdherent(),
                a.getMode().name(), a.getDateAttribution().toString(), a.getDetails() == null ? "" : a.getDetails());
    }

    public boolean verifier(Attribution a) {
        String canon = canonical(a);
        return signatures.hmac(canon).equals(a.getPvSignature()) && signatures.sha256(canon).equals(a.getPvHash());
    }

    @Transactional(readOnly = true)
    public Attribution get(Long id) {
        return attributions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attribution introuvable"));
    }

    @Transactional(readOnly = true)
    public List<AttributionDto> list(Membre caller, boolean bureau) {
        List<Attribution> l = bureau ? attributions.findAllByOrderByDateAttributionDesc()
                : attributions.findByBeneficiaireIdOrderByDateAttributionDesc(caller.getId());
        return l.stream().map(Mapper::attribution).toList();
    }
}
