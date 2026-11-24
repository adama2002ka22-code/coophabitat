import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  template: `
  <div class="landing">
    <!-- Barre de navigation vitrine -->
    <header class="landing-nav">
      <div class="landing-nav-in">
        <a routerLink="/" class="brand-logo">
          <span class="brand-mark" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 10.5L12 3l9 7.5"/><path d="M5 9.5V20h14V9.5"/><path d="M9.5 20v-6h5v6"/>
            </svg>
          </span>
          <span>CoopHabitat <span style="color:#5eead4;font-weight:600">Sénégal</span></span>
        </a>

        <nav class="landing-links" aria-label="Navigation principale">
          <a href="#projets">Nos projets</a>
          <a href="#problematique">Pourquoi CoopHabitat</a>
          <a href="#fonctionnalites">Fonctionnalités</a>
          <a href="#parcours">Comment ça marche</a>
          <a href="#roles">Rôles &amp; Gouvernance</a>
        </nav>

        <div class="landing-cta-group">
          <a routerLink="/login" class="l-btn l-btn-ghost">Se connecter</a>
          <a routerLink="/register" class="l-btn l-btn-primary">Adhérer</a>
        </div>
      </div>
    </header>

    <!-- HERO SECTION AVEC VISUEL IMMOBILIER -->
    <section class="hero-wrap">
      <div class="hero-inner">
        <div>
          <span class="hero-eyebrow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            Coopérative d'Habitat Numérique · Sénégal
          </span>
          <h1 class="hero-title">
            Votre projet immobilier mérite une gestion <span>100 % transparente.</span>
          </h1>
          <p class="hero-sub">
            CoopHabitat digitalise intégralement votre coopérative d'habitat :
            cotisations mensuelles via <b>Wave &amp; SendFlash</b>, reçus PDF automatiques,
            suivi public du <b>séquestre commun</b>, attribution auditable des terrains et logements
            avec <b>PV signés numériquement</b> et assemblées générales numériques.
          </p>

          <div class="hero-actions">
            <a routerLink="/login" class="l-btn l-btn-primary l-btn-lg">
              Accéder à mon espace
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M5 12h14"/><path d="M13 5l7 7-7 7"/></svg>
            </a>
            <a routerLink="/register" class="l-btn l-btn-ghost l-btn-lg">Devenir adhérent</a>
          </div>

          <div class="hero-trust">
            <span><i>✓</i> Séquestre visible par tous</span>
            <span><i>✓</i> Rappels automatiques J-3</span>
            <span><i>✓</i> PV signés PKCS#12 X.509</span>
            <span><i>✓</i> Mobile-first &amp; Hors ligne (PWA)</span>
          </div>
        </div>

        <!-- Photo immobilière haute définition + garanties -->
        <div class="hero-showcase">
          <div class="hero-photo-card">
            <img src="/images/hero-residence.jpg" alt="Immeubles résidentiels et villas modernes CoopHabitat au Sénégal">
            <div class="hero-photo-overlay">
              <div>
                <b>Résidences &amp; Programmes CoopHabitat</b>
                <span>Dakar · Cité Keur Gorgui · Diamniadio · Les Almadies</span>
              </div>
              <span class="hero-photo-badge">Foncier sécurisé</span>
            </div>
          </div>

          <div class="glass-strip">
            <div class="glass-pill">
              <b>Reçus &amp; PV vérifiables</b>
              <span>PDF JasperReports avec signature X.509 &amp; empreinte SHA-256.</span>
            </div>
            <div class="glass-pill">
              <b>Séquestre transparent</b>
              <span>Encaissements et dépenses VRD visibles par tous les membres.</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- GALERIE IMMOBILIERE : NOS PROGRAMMES -->
    <section id="projets" class="l-section">
      <div class="l-head">
        <span class="l-kicker">Programmes immobiliers &amp; fonciers</span>
        <h2>Des immeubles modernes, des villas familiales et des parcelles viabilisées</h2>
        <p>Chaque terrain ou logement de la coopérative est référencé avec sa superficie, sa localisation GPS exacte sur carte et son statut d'attribution audité.</p>
      </div>

      <div class="gallery-grid">
        <article class="project-card">
          <img src="/images/hero-residence.jpg" alt="Immeuble résidentiel moderne CoopHabitat">
          <div class="project-card-body">
            <div class="project-meta"><span>Logements collectifs</span><span>Dakar · Almadies</span></div>
            <h3>Immeubles &amp; Résidences modernes</h3>
            <p>Appartements lumineux avec balcons vitrés, espaces verts aménagés et gestion transparente des appels de fonds de construction.</p>
          </div>
        </article>

        <article class="project-card">
          <img src="/images/villa-projet.jpg" alt="Villa familiale contemporaine CoopHabitat">
          <div class="project-card-body">
            <div class="project-meta"><span>Villas familiales</span><span>Cité Keur Gorgui</span></div>
            <h3>Villas contemporaines clé en main</h3>
            <p>Architecture moderne pensée pour les familles sénégalaises, voirie pavée, suivi des travaux VRD et PV d'attribution certifié.</p>
          </div>
        </article>

        <article class="project-card">
          <img src="/images/lotissement-vue.jpg" alt="Lotissement moderne et parcelles viabilisées">
          <div class="project-card-body">
            <div class="project-meta"><span>Terrains 250–400 m²</span><span>Extension Diamniadio</span></div>
            <h3>Lotissements &amp; Parcelles géolocalisées</h3>
            <p>Parcelles bornées et viabilisées avec coordonnées GPS vérifiables sur OpenStreetMap et attribution équitable par tirage au sort.</p>
          </div>
        </article>
      </div>
    </section>

    <!-- PROBLEMATIQUE -> SOLUTION -->
    <section id="problematique" class="l-section-alt">
      <div class="l-section">
        <div class="l-head">
          <span class="l-kicker">Pourquoi CoopHabitat ?</span>
          <h2>Mettre fin aux carnets papier, aux retards oubliés et aux litiges fonciers</h2>
          <p>
            Les coopératives d'habitat permettent aux fonctionnaires et travailleurs du secteur formel
            d'accéder au logement. CoopHabitat remplace la gestion manuelle opaque par une gouvernance numérique vérifiable par tous.
          </p>
        </div>
        <div class="l-grid-4">
          <div class="l-card">
            <div class="l-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            </div>
            <h3>Fini l'opacité financière</h3>
            <p>Le compte séquestre commun, le budget prévisionnel et chaque dépense sont consultables à tout moment par tous les membres.</p>
          </div>
          <div class="l-card">
            <div class="l-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            </div>
            <h3>Zéro oubli d'échéance</h3>
            <p>Rappel automatique SMS &amp; e-mail 3 jours avant chaque échéance et calcul automatique des pénalités de retard.</p>
          </div>
          <div class="l-card">
            <div class="l-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 12l2 2 4-4"/><path d="M12 2l7 4v6c0 5-3.5 9.5-7 10-3.5-.5-7-5-7-10V6l7-4z"/></svg>
            </div>
            <h3>Attributions incontestables</h3>
            <p>Tirage au sort cryptographique (SecureRandom) ou commission avec journal d'audit complet et PV PDF signé PKCS#12.</p>
          </div>
          <div class="l-card">
            <div class="l-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
            </div>
            <h3>Mobile-first &amp; Hors ligne</h3>
            <p>Application PWA (Service Worker) : consultez votre solde, calendrier et documents même en cas de coupure internet.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 6 MODULES CLES -->
    <section id="fonctionnalites" class="l-section">
      <div class="l-head">
        <span class="l-kicker">Tout-en-un · 6 modules intégrés</span>
        <h2>Une plateforme complète qui couvre chaque étape de la coopérative</h2>
        <p>Du premier versement d'adhésion jusqu'à la remise du procès-verbal d'attribution signé et aux rapports financiers semestriels.</p>
      </div>

      <div class="l-grid-3">
        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          </div>
          <h3>Membres &amp; Plan de cotisation</h3>
          <p>Numéro d'adhérent unique (ADH-AAAA-NNN), types (Fondateur, Ordinaire, Bénéficiaire), suspension sécurisée et règles de pénalités configurables.</p>
          <span class="l-tag">Calcul auto + Rappels J-3</span>
        </div>

        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>
          </div>
          <h3>Mobile Money &amp; Reçus JasperReports</h3>
          <p>Paiement en ligne via <b>Wave</b> et <b>SendFlash</b> (sandbox) ou saisie manuelle trésorier, imputation automatique et reçu PDF immédiat.</p>
          <span class="l-tag">Wave · SendFlash · Reçu PDF</span>
        </div>

        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
          </div>
          <h3>Gestion foncière &amp; Lots GPS</h3>
          <p>Inventaire complet des terrains et logements : superficie, localisation, carte GPS interactive, statuts Disponible / Attribué / Litigieux.</p>
          <span class="l-tag">Carte GPS intégrée</span>
        </div>

        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><path d="M9 15l2 2 4-4"/></svg>
          </div>
          <h3>Attributions &amp; PV signés PKCS#12</h3>
          <p>Seuls les membres actifs à jour sans lot participent au tirage au sort. Un PV PDF signé cryptographiquement est généré et notifié immédiatement.</p>
          <span class="l-tag">Signature X.509 + Audit</span>
        </div>

        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
          </div>
          <h3>Comptabilité, Séquestre &amp; Exports</h3>
          <p>Tableau de bord trésorier en temps réel, suivi budget prévisionnel vs dépenses réelles, séquestre public et export semestriel en <b>PDF &amp; Excel</b>.</p>
          <span class="l-tag">PDF JasperReports &amp; XLSX</span>
        </div>

        <div class="l-card">
          <div class="l-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          </div>
          <h3>Assemblée Générale Numérique</h3>
          <p>Convocation envoyée automatiquement à tous les membres, ordre du jour, enregistrement des votes (Pour / Contre / Abstention) et comptes rendus.</p>
          <span class="l-tag">Convocations · Votes · CR</span>
        </div>
      </div>
    </section>

    <!-- COMMENT CA MARCHE -->
    <section id="parcours" class="l-section-alt">
      <div class="l-section">
        <div class="l-head">
          <span class="l-kicker">Parcours adhérent en 4 étapes</span>
          <h2>Comment fonctionne CoopHabitat au quotidien ?</h2>
          <p>Un processus fluide, de votre adhésion jusqu'à l'obtention de votre lot.</p>
        </div>
        <div class="l-grid-4">
          <div class="l-card">
            <span class="step-num">1</span>
            <h3>Inscription sécurisée</h3>
            <p>Créez votre compte membre et recevez instantanément votre numéro d'adhérent unique et votre calendrier de cotisations.</p>
          </div>
          <div class="l-card">
            <span class="step-num">2</span>
            <h3>Paiement &amp; Reçu auto</h3>
            <p>Réglez vos mensualités via Wave ou SendFlash (ou au bureau) et téléchargez immédiatement votre reçu officiel PDF.</p>
          </div>
          <div class="l-card">
            <span class="step-num">3</span>
            <h3>Suivi en temps réel</h3>
            <p>Consultez votre solde dû, le fonds commun en séquestre, les dépenses VRD/Foncier et les comptes rendus d'AG.</p>
          </div>
          <div class="l-card">
            <span class="step-num">4</span>
            <h3>Attribution &amp; PV signé</h3>
            <p>En tant que membre à jour, participez aux tirages transparents et recevez votre PV d'attribution PDF signé numériquement.</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 4 ROLES -->
    <section id="roles" class="l-section">
      <div class="l-head">
        <span class="l-kicker">Gouvernance &amp; Sécurité JWT (RBAC)</span>
        <h2>Un espace adapté et protégé pour chacun des 4 rôles</h2>
        <p>Chaque utilisateur se connecte avec son email et son mot de passe et accède uniquement aux modules autorisés pour son rôle.</p>
      </div>
      <div class="l-grid-4">
        <div class="l-card">
          <h3>Président (Vue globale)</h3>
          <ul class="role-list">
            <li>Vision complète sur tous les modules</li>
            <li>Attribution par tirage ou commission</li>
            <li>Nomination des rôles du bureau</li>
            <li>Consultation du journal d'audit</li>
          </ul>
        </div>
        <div class="l-card">
          <h3>Trésorier</h3>
          <ul class="role-list">
            <li>Tableau de bord à jour / en retard</li>
            <li>Plan de cotisation &amp; pénalités</li>
            <li>Saisie paiements, budget &amp; dépenses</li>
            <li>Rappels J-3 &amp; exports PDF / Excel</li>
          </ul>
        </div>
        <div class="l-card">
          <h3>Secrétaire</h3>
          <ul class="role-list">
            <li>Inscription &amp; suspension des membres</li>
            <li>Gestion des lots &amp; statut litigieux</li>
            <li>Organisation des tirages au sort</li>
            <li>Convocations AG, votes &amp; comptes rendus</li>
          </ul>
        </div>
        <div class="l-card">
          <h3>Membre Adhérent</h3>
          <ul class="role-list">
            <li>Solde dû &amp; historique complet en direct</li>
            <li>Paiement Wave / SendFlash + reçu PDF</li>
            <li>Accès transparent au séquestre commun</li>
            <li>Téléchargement PV &amp; documents officiels</li>
          </ul>
        </div>
      </div>
    </section>

    <!-- CTA FINAL -->
    <section class="l-section">
      <div class="l-cta-banner">
        <div>
          <h2 style="color:#fff;margin-bottom:.35rem">Prêt à explorer la plateforme CoopHabitat ?</h2>
          <p style="color:#cbd5e1;margin:0;max-width:580px">
            Connectez-vous avec votre email et mot de passe pour accéder à l'espace correspondant à votre rôle.
          </p>
        </div>
        <div class="landing-cta-group">
          <a routerLink="/login" class="l-btn l-btn-primary l-btn-lg">Se connecter →</a>
          <a routerLink="/register" class="l-btn l-btn-ghost l-btn-lg">Créer un compte</a>
        </div>
      </div>
    </section>

    <footer class="l-footer">
      <div class="l-footer-in">
        <span>© 2026 CoopHabitat Sénégal — Gestion numérique transparente des coopératives d'habitat.</span>
        <div style="display:flex;gap:1.2rem;flex-wrap:wrap">
          <a routerLink="/login" style="color:#94a3b8">Connexion</a>
          <a routerLink="/register" style="color:#94a3b8">Adhésion</a>
          <span style="color:#5eead4">Spring Boot 3 · Angular PWA · PostgreSQL · JasperReports</span>
        </div>
      </div>
    </footer>
  </div>
  `
})
export class LandingComponent {}
