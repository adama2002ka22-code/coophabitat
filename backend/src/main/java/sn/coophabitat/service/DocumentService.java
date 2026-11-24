package sn.coophabitat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.coophabitat.domain.Membre;
import sn.coophabitat.dto.Dtos.AssembleeDto;
import sn.coophabitat.dto.Dtos.AttributionDto;
import sn.coophabitat.dto.Dtos.DocumentDto;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Documents officiels accessibles dans l'espace membre. */
@Service
public class DocumentService {
    private final AssembleeService assemblees;
    private final AttributionService attributions;
    private final Clock clock;

    public DocumentService(AssembleeService assemblees, AttributionService attributions, Clock clock) {
        this.assemblees = assemblees; this.attributions = attributions; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> list(Membre m, boolean bureau) {
        List<DocumentDto> docs = new ArrayList<>();
        LocalDate today = LocalDate.now(clock);
        int semestre = today.getMonthValue() <= 6 ? 1 : 2;
        docs.add(new DocumentDto("Rapport financier semestriel " + today.getYear() + " - S" + semestre, "RAPPORT", today,
                "/rapports/financier/pdf?annee=" + today.getYear() + "&semestre=" + semestre));
        for (AttributionDto a : attributions.list(m, bureau))
            docs.add(new DocumentDto("PV d'attribution " + a.pvNumero() + " - " + a.lotReference(), "PV",
                    a.dateAttribution().toLocalDate(), "/attributions/" + a.id() + "/pv"));
        for (AssembleeDto ag : assemblees.list())
            if (ag.compteRenduPublie())
                docs.add(new DocumentDto("Compte rendu - " + ag.titre(), "COMPTE_RENDU", ag.dateAssemblee().toLocalDate(), "/assemblees/" + ag.id()));
        return docs;
    }
}
