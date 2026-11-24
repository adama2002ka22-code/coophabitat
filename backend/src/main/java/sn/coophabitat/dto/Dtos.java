package sn.coophabitat.dto;

import jakarta.validation.constraints.*;
import sn.coophabitat.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    public record RegisterRequest(@NotBlank String nom, @NotBlank @Email String email, String telephone,
                                  @NotBlank @Size(min = 8, message = "8 caracteres minimum") String motDePasse,
                                  TypeMembre typeMembre) {}
    public record CreateMembreRequest(@NotBlank String nom, @NotBlank @Email String email, String telephone,
                                      @NotBlank @Size(min = 8, message = "8 caracteres minimum") String motDePasse,
                                      @NotNull TypeMembre typeMembre, @NotNull Role role) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String motDePasse) {}
    public record StatutRequest(@NotNull StatutMembre statut) {}
    public record MembreResponse(Long id, String numeroAdherent, String nom, String email, String telephone,
                                 TypeMembre typeMembre, StatutMembre statut, Role role, LocalDate dateAdhesion,
                                 String statutCotisation) {}
    public record AuthResponse(String token, String type, MembreResponse membre) {}

    public record PlanRequest(@NotNull @DecimalMin("1") BigDecimal montantMensuel, @Min(1) @Max(28) int jourEcheance,
                              @NotNull @DecimalMin("0") BigDecimal tauxPenaliteJournalier,
                              @NotNull @DecimalMin("0") BigDecimal penaliteFixe, @Min(0) int delaiGraceJours) {}
    public record PlanResponse(Long id, BigDecimal montantMensuel, int jourEcheance, BigDecimal tauxPenaliteJournalier,
                               BigDecimal penaliteFixe, int delaiGraceJours, LocalDate dateEffet) {}

    public record EcheanceDto(Long id, LocalDate dateEcheance, BigDecimal montantDu, BigDecimal penalite,
                              BigDecimal montantPaye, BigDecimal solde, String statut) {}
    public record PaiementDto(Long id, Long membreId, String membreNom, String numeroAdherent, BigDecimal montant,
                              CanalPaiement canal, String reference, String recuNumero, LocalDateTime datePaiement) {}
    public record Situation(Long membreId, String nom, String numeroAdherent, BigDecimal totalPaye,
                            BigDecimal soldeRestantDu, BigDecimal penalitesTotales, String statutCotisation,
                            LocalDate prochaineEcheance, List<EcheanceDto> calendrier, List<PaiementDto> paiements) {}
    public record RetardataireDto(Long membreId, String nom, String numeroAdherent, String telephone,
                                  BigDecimal soldeRestantDu, long joursRetardMax) {}
    public record PaiementManuelRequest(@NotNull Long membreId, @NotNull @DecimalMin("1") BigDecimal montant,
                                        @NotNull CanalPaiement canal, String reference) {}
    public record MobileMoneyRequest(@NotBlank String telephone, @NotNull @DecimalMin("1") BigDecimal montant,
                                     @NotBlank String operateur) {}

    public record LotRequest(@NotBlank String reference, @NotNull TypeLot typeLot, @Positive double superficie,
                             String localisation, Double latitude, Double longitude) {}
    public record LotStatutRequest(@NotNull StatutLot statut) {}
    public record LotDto(Long id, String reference, TypeLot typeLot, double superficie, String localisation,
                         Double latitude, Double longitude, StatutLot statut, String beneficiaire, Long attributionId) {}
    public record CommissionRequest(@NotNull Long membreId, @NotBlank String membresCommission,
                                    @NotBlank String justification) {}
    public record AttributionDto(Long id, Long lotId, String lotReference, Long beneficiaireId, String beneficiaireNom,
                                 String beneficiaireNumero, ModeAttribution mode, String details,
                                 LocalDateTime dateAttribution, String effectuePar, String pvNumero, String pvHash) {}
    public record VerificationDto(String pvNumero, boolean valide, String hash) {}

    public record BudgetLigneRequest(@Min(2000) int exercice, @NotBlank String categorie, @NotBlank String libelle,
                                     @NotNull @DecimalMin("0") BigDecimal montantPrevu) {}
    public record BudgetLigneDto(Long id, int exercice, String categorie, String libelle, BigDecimal montantPrevu) {}
    public record DepenseRequest(@NotBlank String categorie, @NotBlank String libelle,
                                 @NotNull @DecimalMin("1") BigDecimal montant, LocalDate dateDepense) {}
    public record DepenseDto(Long id, LocalDate dateDepense, String categorie, String libelle, BigDecimal montant,
                             String enregistrePar) {}
    public record TresorerieDto(BigDecimal totalEncaisse, BigDecimal totalDepenses, BigDecimal tresorerieDisponible) {}
    public record DashboardDto(long membresActifs, long membresAJour, long membresEnRetard, BigDecimal totalImpayes,
                               TresorerieDto tresorerie, List<RetardataireDto> retardataires) {}
    public record RapportFinancier(int annee, int semestre, LocalDate debut, LocalDate fin,
                                   BigDecimal budgetPrevisionnel, List<BudgetLigneDto> lignesBudget,
                                   BigDecimal encaissements, BigDecimal depenses,
                                   Map<String, BigDecimal> depensesParCategorie, BigDecimal tresorerieDisponible,
                                   long membresAJour, long membresEnRetard) {}

    public record AssembleeRequest(@NotBlank String titre, @NotNull LocalDateTime dateAssemblee, String lieu,
                                   @NotBlank String ordreDuJour) {}
    public record DeliberationRequest(@NotBlank String titre, @NotBlank String resolution, @Min(0) int pour,
                                      @Min(0) int contre, @Min(0) int abstention) {}
    public record DeliberationDto(Long id, String titre, String resolution, int pour, int contre, int abstention,
                                  boolean adoptee) {}
    public record CompteRenduRequest(@NotBlank String compteRendu, boolean publier) {}
    public record AssembleeDto(Long id, String titre, LocalDateTime dateAssemblee, String lieu, String ordreDuJour,
                               StatutAssemblee statut, String compteRendu, boolean compteRenduPublie,
                               List<DeliberationDto> deliberations) {}

    public record NotificationDto(Long id, String sujet, String message, LocalDateTime dateCreation, boolean lu) {}
    public record DocumentDto(String titre, String type, LocalDate date, String url) {}
    public record AuditDto(Long id, LocalDateTime dateAction, String acteur, String action, String entite,
                           Long entiteId, String details) {}
}
