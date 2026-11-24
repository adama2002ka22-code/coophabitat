# Diagrammes UML (Mermaid - s'affichent directement sur GitHub / GitLab)

## Cas d'utilisation
```mermaid
flowchart LR
  M([Membre]) --- UC1(Consulter solde, calendrier, historique)
  M --- UC2(Payer par Wave / SendFlash)
  M --- UC3(Telecharger recus et PV)
  M --- UC4(Consulter sequestre, budget, depenses)
  M --- UC5(Consulter AG, comptes rendus)
  T([Tresorier]) --- UC6(Saisir paiements et depenses)
  T --- UC7(Definir plan et penalites)
  T --- UC8(Tableau de bord, rappels J-3)
  T --- UC9(Exporter rapports PDF / Excel)
  S([Secretaire]) --- UC10(Inscrire / suspendre des membres)
  S --- UC11(Convoquer AG, deliberations, compte rendu)
  S --- UC12(Gerer les lots, tirage au sort)
  P([President]) --- UC10
  P --- UC11
  P --- UC12
  P --- UC13(Attribution par commission)
  P --- UC14(Attribuer les roles du bureau)
  P --- UC8
```

## Diagramme de classes
```mermaid
classDiagram
  class Membre{numeroAdherent; nom; email; telephone; typeMembre; statut; role; dateAdhesion}
  class PlanCotisation{montantMensuel; jourEcheance; tauxPenaliteJournalier; penaliteFixe; delaiGraceJours; actif}
  class Cotisation{dateEcheance; montantDu; penalite; montantPaye; rappelEnvoye; getSolde()}
  class Paiement{montant; canal; reference; recuNumero; datePaiement}
  class Lot{reference; typeLot; superficie; latitude; longitude; statut}
  class Attribution{mode; details; dateAttribution; pvNumero; pvHash; pvSignature}
  class Assemblee{titre; dateAssemblee; ordreDuJour; statut; compteRendu; compteRenduPublie}
  class Deliberation{titre; resolution; pour; contre; abstention}
  class BudgetLigne{exercice; categorie; montantPrevu}
  class Depense{dateDepense; categorie; montant}
  class Notification{sujet; message; lu}
  class AuditLog{dateAction; acteur; action; details}
  Membre "1" --> "*" Cotisation
  Membre "1" --> "*" Paiement
  Membre "1" --> "0..1" Attribution : beneficiaire
  Lot "1" --> "0..1" Attribution
  Assemblee "1" --> "*" Deliberation
  Membre "1" --> "*" Notification
  PlanCotisation ..> Cotisation : genere
```

## Sequence : paiement Mobile Money
```mermaid
sequenceDiagram
  actor M as Membre
  participant F as Front Angular
  participant A as API PaiementController
  participant G as PaymentGateway
  participant S as PaiementService
  M->>F: Payer 50 000 FCFA (Wave)
  F->>A: POST /api/paiements/mobile-money (JWT)
  A->>S: mobileMoney(membre, requete)
  S->>S: verifier montant <= solde restant
  S->>G: charger(telephone, montant)
  G-->>S: succes + reference
  S->>S: imputer sur les echeances, creer paiement, numero de recu
  S-->>A: PaiementDto
  A-->>F: 201 + recu
  F->>A: GET /api/paiements/{id}/recu
  A-->>F: PDF genere par JasperReports
```

## Sequence : tirage au sort et PV
```mermaid
sequenceDiagram
  actor P as President
  participant A as LotController
  participant S as AttributionService
  participant N as NotificationService
  P->>A: POST /api/lots/{id}/tirage
  A->>S: tirageAuSort(lot)
  S->>S: eligibles = actifs, a jour, sans lot
  S->>S: SecureRandom -> beneficiaire
  S->>S: PV : SHA-256 + HMAC-SHA256, audit
  S->>N: notifier beneficiaire et membres (in-app, e-mail, SMS)
  S-->>A: AttributionDto
  P->>A: GET /api/attributions/{id}/pv
  A-->>P: PDF JasperReports signe PKCS#12 (X.509 RSA-SHA256)
```

## Sequence : rappel J-3
```mermaid
sequenceDiagram
  participant C as Scheduler (7h, Dakar)
  participant S as CotisationService
  participant N as NotificationService
  C->>S: genererEcheances / rafraichirPenalites
  C->>S: envoyerRappels()
  S->>S: echeances = aujourd'hui + 3 jours, solde > 0, rappel non envoye
  S->>N: notifier(membre) -> e-mail + SMS + in-app
  S->>S: rappelEnvoye = true
```
