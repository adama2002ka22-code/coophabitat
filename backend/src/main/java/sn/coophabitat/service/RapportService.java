package sn.coophabitat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sn.coophabitat.domain.Depense;
import sn.coophabitat.dto.Dtos.*;
import sn.coophabitat.dto.Mapper;
import sn.coophabitat.repository.DepenseRepository;
import sn.coophabitat.repository.PaiementRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RapportService {
    private final PaiementRepository paiements;
    private final DepenseRepository depenses;
    private final ComptabiliteService compta;

    public RapportService(PaiementRepository paiements, DepenseRepository depenses, ComptabiliteService compta) {
        this.paiements = paiements; this.depenses = depenses; this.compta = compta;
    }

    @Transactional
    public RapportFinancier semestriel(int annee, int semestre) {
        if (semestre != 1 && semestre != 2) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semestre : 1 ou 2");
        LocalDate debut = semestre == 1 ? LocalDate.of(annee, 1, 1) : LocalDate.of(annee, 7, 1);
        LocalDate fin = semestre == 1 ? LocalDate.of(annee, 6, 30) : LocalDate.of(annee, 12, 31);
        List<BudgetLigneDto> lignes = compta.budget(annee);
        BigDecimal prevu = lignes.stream().map(BudgetLigneDto::montantPrevu).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal enc = Mapper.nz(paiements.totalEntre(debut.atStartOfDay(), fin.plusDays(1).atStartOfDay()));
        Map<String, BigDecimal> parCat = new LinkedHashMap<>();
        BigDecimal dep = BigDecimal.ZERO;
        for (Depense d : depenses.findByDateDepenseBetween(debut, fin)) {
            parCat.merge(d.getCategorie(), d.getMontant(), BigDecimal::add);
            dep = dep.add(d.getMontant());
        }
        DashboardDto dash = compta.dashboard();
        return new RapportFinancier(annee, semestre, debut, fin, prevu, lignes, enc, dep, parCat,
                dash.tresorerie().tresorerieDisponible(), dash.membresAJour(), dash.membresEnRetard());
    }
}
