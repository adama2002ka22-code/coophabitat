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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LotService {
    private final LotRepository lots;
    private final AttributionRepository attributions;
    private final AuditService audit;

    public LotService(LotRepository lots, AttributionRepository attributions, AuditService audit) {
        this.lots = lots; this.attributions = attributions; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<LotDto> list() {
        Map<Long, Attribution> parLot = attributions.findAll().stream()
                .collect(Collectors.toMap(a -> a.getLot().getId(), Function.identity()));
        return lots.findAll().stream().map(l -> Mapper.lot(l, parLot.get(l.getId()))).toList();
    }

    @Transactional
    public LotDto create(LotRequest r, String acteur) {
        if (lots.existsByReferenceIgnoreCase(r.reference()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reference de lot deja utilisee");
        Lot l = new Lot();
        l.setReference(r.reference().trim());
        l.setTypeLot(r.typeLot());
        l.setSuperficie(r.superficie());
        l.setLocalisation(r.localisation());
        l.setLatitude(r.latitude());
        l.setLongitude(r.longitude());
        l = lots.save(l);
        audit.log(acteur, "LOT_CREE", "Lot", l.getId(), l.getReference() + " (" + l.getSuperficie() + " m2)");
        return Mapper.lot(l, null);
    }

    @Transactional
    public LotDto changerStatut(Long id, StatutLot statut, String acteur) {
        Lot l = lots.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot introuvable"));
        if (l.getStatut() == StatutLot.ATTRIBUE)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un lot attribue ne peut pas changer de statut");
        if (statut == StatutLot.ATTRIBUE)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le statut ATTRIBUE passe uniquement par le workflow d'attribution");
        l.setStatut(statut);
        audit.log(acteur, "LOT_STATUT", "Lot", id, "statut=" + statut);
        return Mapper.lot(l, null);
    }
}
