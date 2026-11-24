package sn.coophabitat.dto;

import sn.coophabitat.domain.*;
import sn.coophabitat.dto.Dtos.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Mapper {
    private Mapper() {}

    public static BigDecimal nz(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }

    public static MembreResponse membre(Membre m, boolean enRetard) {
        return new MembreResponse(m.getId(), m.getNumeroAdherent(), m.getNom(), m.getEmail(), m.getTelephone(),
                m.getTypeMembre(), m.getStatut(), m.getRole(), m.getDateAdhesion(), enRetard ? "EN_RETARD" : "A_JOUR");
    }

    public static PlanResponse plan(PlanCotisation p) {
        return new PlanResponse(p.getId(), p.getMontantMensuel(), p.getJourEcheance(), p.getTauxPenaliteJournalier(),
                p.getPenaliteFixe(), p.getDelaiGraceJours(), p.getDateEffet());
    }

    public static EcheanceDto echeance(Cotisation c, LocalDate today) {
        BigDecimal solde = c.getSolde();
        String statut;
        if (solde.signum() <= 0) statut = "PAYEE";
        else if (c.getDateEcheance().isBefore(today)) statut = "EN_RETARD";
        else if (c.getMontantPaye().signum() > 0) statut = "PARTIELLE";
        else statut = "A_VENIR";
        return new EcheanceDto(c.getId(), c.getDateEcheance(), c.getMontantDu(), c.getPenalite(), c.getMontantPaye(),
                solde.max(BigDecimal.ZERO), statut);
    }

    public static PaiementDto paiement(Paiement p) {
        return new PaiementDto(p.getId(), p.getMembre().getId(), p.getMembre().getNom(),
                p.getMembre().getNumeroAdherent(), p.getMontant(), p.getCanal(), p.getReference(), p.getRecuNumero(),
                p.getDatePaiement());
    }

    public static LotDto lot(Lot l, Attribution a) {
        return new LotDto(l.getId(), l.getReference(), l.getTypeLot(), l.getSuperficie(), l.getLocalisation(),
                l.getLatitude(), l.getLongitude(), l.getStatut(), a == null ? null : a.getBeneficiaire().getNom(),
                a == null ? null : a.getId());
    }

    public static AttributionDto attribution(Attribution a) {
        return new AttributionDto(a.getId(), a.getLot().getId(), a.getLot().getReference(),
                a.getBeneficiaire().getId(), a.getBeneficiaire().getNom(), a.getBeneficiaire().getNumeroAdherent(),
                a.getMode(), a.getDetails(), a.getDateAttribution(), a.getEffectuePar(), a.getPvNumero(), a.getPvHash());
    }

    public static BudgetLigneDto budget(BudgetLigne b) {
        return new BudgetLigneDto(b.getId(), b.getExercice(), b.getCategorie(), b.getLibelle(), b.getMontantPrevu());
    }

    public static DepenseDto depense(Depense d) {
        return new DepenseDto(d.getId(), d.getDateDepense(), d.getCategorie(), d.getLibelle(), d.getMontant(),
                d.getEnregistrePar());
    }

    public static DeliberationDto deliberation(Deliberation d) {
        return new DeliberationDto(d.getId(), d.getTitre(), d.getResolution(), d.getPour(), d.getContre(),
                d.getAbstention(), d.getPour() > d.getContre());
    }

    public static AssembleeDto assemblee(Assemblee a, List<Deliberation> ds) {
        return new AssembleeDto(a.getId(), a.getTitre(), a.getDateAssemblee(), a.getLieu(), a.getOrdreDuJour(),
                a.getStatut(), a.isCompteRenduPublie() ? a.getCompteRendu() : null, a.isCompteRenduPublie(),
                ds.stream().map(Mapper::deliberation).toList());
    }

    public static NotificationDto notification(Notification n) {
        return new NotificationDto(n.getId(), n.getSujet(), n.getMessage(), n.getDateCreation(), n.isLu());
    }

    public static AuditDto audit(AuditLog a) {
        return new AuditDto(a.getId(), a.getDateAction(), a.getActeur(), a.getAction(), a.getEntite(),
                a.getEntiteId(), a.getDetails());
    }
}
