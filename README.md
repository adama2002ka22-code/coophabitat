# CoopHabitat — Gestion numérique d'une coopérative d'habitat

Stack : **Spring Boot 3 (Java 17/21) · Angular 20 (PWA) · PostgreSQL 16 · JasperReports 6.20 · Apache POI**.

```
backend/    API REST Spring Boot (JWT, RBAC 4 roles, OpenAPI 3, JasperReports + PKCS#12, JUnit + JaCoCo >= 60 %)
frontend/   Application Angular mobile-first + Service Worker (mode hors ligne partiel)
docs/       UML (Mermaid), collection Postman, spec OpenAPI, guide de deploiement
```

---

## Démarrage en local (en moins de 5 minutes)

### Prérequis
- Java 17+ (testé avec JDK 21)
- Node.js 20+ & npm
- Docker Desktop (pour PostgreSQL 16)

### 1. Base de données PostgreSQL
```bash
cd backend
docker compose up -d db       # PostgreSQL 16 sur localhost:5432
```

### 2. Back-end (Spring Boot)
```bash
cd backend
# Optionnel : copier .env.example vers .env et adapter les variables
./mvnw spring-boot:run        # Linux / macOS
.\mvnw.cmd spring-boot:run    # Windows (PowerShell / CMD)
```
- **API REST** : `http://localhost:8080`
- **Swagger UI (OpenAPI 3.0)** : `http://localhost:8080/swagger-ui.html`
- **Spec OpenAPI YAML** : `http://localhost:8080/v3/api-docs.yaml` (exportée dans `docs/openapi.yaml`)
- **Collection Postman** : `docs/CoopHabitat.postman_collection.json`

### 3. Front-end (Angular Mobile-First + PWA)
```bash
cd frontend
npm install
npm start                     # http://localhost:4200
```
*(Pour tester le Service Worker / mode hors ligne : `npm run build` puis servir `dist/frontend/browser`.)*

---

## Comptes de démonstration
Créés automatiquement au premier démarrage (`SEED_ENABLED=true`).  
Mot de passe par défaut : **`ChangeMe123!`** (`SEED_PASSWORD`).

| Rôle | Email | Type | Statut initial |
| :--- | :--- | :--- | :--- |
| **Président** | `president@coophabitat.sn` | Fondateur | À jour |
| **Trésorier** | `tresorier@coophabitat.sn` | Fondateur | À jour |
| **Secrétaire** | `secretaire@coophabitat.sn` | Ordinaire | À jour |
| **Membre** | `coumbel.ka@coophabitat.sn` | Ordinaire | À jour |
| **Membre** | `abdoulaye.d@coophabitat.sn` | Bénéficiaire | **En retard** (avec pénalités) |

---

## Variables d'environnement (Back-end)
Voir [`backend/.env.example`](backend/.env.example).

| Variable | Défaut | Rôle |
| :--- | :--- | :--- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/coophabitat` / `coophabitat` | Connexion PostgreSQL (URL **JDBC**) |
| `JWT_SECRET` | valeur de dev | Clé secrète HMAC JWT (≥ 32 car., à changer en production) |
| `JWT_EXPIRATION_MINUTES` | `120` | Durée de validité du jeton JWT |
| `PV_SECRET` | `= JWT_SECRET` | Clé de scellement HMAC-SHA256 des PV d'attribution |
| `PV_KEYSTORE_PATH` | `classpath:keystore/coophabitat-pv.p12` | Certificat X.509 PKCS#12 pour signature numérique PDF |
| `PV_KEYSTORE_PASSWORD` / `PV_KEYSTORE_ALIAS` | `coophabitat-pv-2026` / `coophabitat-pv` | Mot de passe et alias du keystore PKCS#12 |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origines front autorisées (séparées par virgules) |
| `SEED_ENABLED` / `SEED_PASSWORD` | `true` / `ChangeMe123!` | Données et comptes de démonstration |
| `PLAN_MONTANT`, `PLAN_JOUR`, `PLAN_TAUX`, `PLAN_FIXE`, `PLAN_GRACE` | `50000`, `5`, `0.1`, `0`, `0` | Plan de cotisation initial |
| `SCHEDULER_CRON` | `0 0 7 * * *` | Tâche quotidienne (échéances, pénalités, rappels J-3), fuseau `Africa/Dakar` |
| `SPRING_MAIL_HOST` / `_PORT` / `_USERNAME` / `_PASSWORD`, `MAIL_FROM` | vide / `no-reply@coophabitat.sn` | SMTP pour l'envoi réel d'e-mails (optionnel) |

---

## Tests et couverture de code (JaCoCo ≥ 60 %)
```bash
cd backend
./mvnw test      # Tests unitaires + tests d'intégration (base H2 en mémoire)
./mvnw verify    # + Rapport JaCoCo (target/site/jacoco/index.html) avec vérification du seuil >= 60 %
```
- **Tests unitaires** : `PenaltyCalculatorTest`, `JwtServiceTest`, `ReminderSchedulerTest` (rappel J-3), `SignatureAndDocumentsTest` (JasperReports, signature cryptographique PKCS#12, Excel).
- **Tests d'intégration** : `ApiIntegrationTest` (auth JWT, RBAC 4 rôles, cotisations, Wave/SendFlash sandbox, tirage au sort / commission, PV signé, séquestre, budget/dépenses, rapports PDF/Excel, assemblées générales).

---

## Correspondance complète avec le cahier des charges

| Exigence | Implémentation |
| :--- | :--- |
| **Membres** (n° adhérent, type, statut) | `Membre`, `/api/membres`, n° `ADH-AAAA-NNN` unique ; suspension bloque login et jeton existant |
| **Plan de cotisation & pénalités** | `PlanCotisation`, `PUT /api/plan-cotisation` (montant, jour échéance, taux %/j, fixe, grâce) |
| **Calcul auto des intérêts de retard** | `PenaltyCalculator` (testé), recalcul à chaque consultation et chaque nuit à 7h |
| **Rappels automatiques J-3 (SMS / e-mail)** | `ReminderScheduler` + `POST /api/cotisations/rappels` (in-app, SMTP e-mail, `SmsSender`) |
| **Paiements manuels & Mobile Money + Reçu PDF** | `PaymentGateway` (`SandboxPaymentGateway` Wave / SendFlash), reçu PDF généré automatiquement par **JasperReports** (`recu_paiement.jrxml`) |
| **Séquestre visible par tous les membres** | `GET /api/tresorerie/sequestre` affiché en temps réel sur l'accueil et l'espace comptable |
| **Lots (superficie, GPS, statut)** | `Lot` (`TERRAIN`/`LOGEMENT`, lat/lng + lien carte OpenStreetMap, `DISPONIBLE`/`ATTRIBUE`/`LITIGIEUX`) |
| **Attribution auditable + PV PDF signé** | `AttributionService` (`SecureRandom` ou commission), journal d'audit, PV PDF **JasperReports** (`pv_attribution.jrxml`) **signé numériquement PKCS#12 (X.509 RSA-SHA256)** + sceau SHA-256/HMAC vérifiable (`/api/attributions/{id}/verification`) + notification bénéficiaire |
| **Espace comptable & exports PDF / Excel** | Budget prévisionnel, dépenses réelles, trésorerie, rapport semestriel **PDF (JasperReports)** et **Excel (Apache POI)** |
| **Assemblée générale numérique** | Convocations (notification auto), ordre du jour, délibérations (votes), comptes rendus publiés |
| **Rôles & Sécurité JWT** | `PRESIDENT`, `TRESORIER`, `SECRETAIRE`, `MEMBRE` via JWT + `@PreAuthorize` + mots de passe BCrypt |
| **Tableau de bord Trésorier temps réel** | `GET /api/dashboard` (membres à jour, en retard, impayés, trésorerie), rafraîchi toutes les 10 s |
| **Mobile-first & mode offline partiel** | CSS responsive (360px+), PWA `@angular/service-worker` (`ngsw-config.json`), cache GET API, bandeau hors ligne |

---

## Documentation & Déploiement
- **Diagrammes UML** : [`docs/UML.md`](docs/UML.md) (cas d'utilisation, classes, séquences)
- **Contrat d'API** : [`docs/openapi.yaml`](docs/openapi.yaml) et [`docs/CoopHabitat.postman_collection.json`](docs/CoopHabitat.postman_collection.json)
- **Déploiement cloud gratuit** : [`docs/DEPLOIEMENT.md`](docs/DEPLOIEMENT.md) (PostgreSQL Neon/Render + API Render + Front Netlify/Vercel)
