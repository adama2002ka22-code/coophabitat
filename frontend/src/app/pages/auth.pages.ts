import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
  <div class="auth">
    <aside class="auth-visual">
      <div>
        <a routerLink="/" class="brand-logo" style="margin-bottom:2rem">
          <span class="brand-mark">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2"><path d="M3 10.5L12 3l9 7.5"/><path d="M5 9.5V20h14V9.5"/><path d="M9.5 20v-6h5v6"/></svg>
          </span>
          <span>CoopHabitat <span style="color:#5eead4">Sénégal</span></span>
        </a>
        <h1>Accédez à votre coopérative en toute confiance.</h1>
        <p>Suivez vos cotisations en temps réel, réglez par Wave ou SendFlash, consultez le séquestre commun et téléchargez vos documents officiels signés.</p>
        <ul class="auth-features">
          <li><span>✓</span> Authentification sécurisée par jeton JWT &amp; rôles métier</li>
          <li><span>✓</span> Reçus PDF générés automatiquement via JasperReports</li>
          <li><span>✓</span> PV d'attribution signés cryptographiquement (PKCS#12)</li>
          <li><span>✓</span> Consultation hors ligne activée (PWA Service Worker)</li>
        </ul>
        <div class="auth-bg-photo">
          <img src="/images/hero-residence.jpg" alt="Résidence moderne CoopHabitat Sénégal">
        </div>
      </div>
      <div style="font-size:.82rem;color:#94a3b8">CoopHabitat Sénégal · Plateforme de gestion transparente</div>
    </aside>

    <main class="auth-panel">
      <div class="box">
        <a routerLink="/" class="back-home">← Retour à l'accueil</a>
        <div class="brand">Connexion</div>
        <p class="muted" style="margin-top:0">Entrez vos identifiants ou cliquez sur un compte de démonstration.</p>

        <div class="muted" style="font-size:.78rem;font-weight:600">Accès rapide démo (pré-remplit email + mot de passe) :</div>
        <div class="demo">
          @for (d of demos; track d.email) {
            <button type="button" (click)="fillDemo(d.email)">{{ d.label }}</button>
          }
        </div>

        <label>Email</label>
        <input type="email" [(ngModel)]="email" placeholder="vous@coophabitat.sn" autocomplete="username">
        <label>Mot de passe</label>
        <input type="password" [(ngModel)]="password" autocomplete="current-password" (keyup.enter)="submit()">

        @if (error) { <div class="msg err">{{ error }}</div> }
        <p style="margin-top:1rem">
          <button class="btn primary" style="width:100%;padding:.7rem" [disabled]="loading" (click)="submit()">
            {{ loading ? 'Connexion en cours...' : 'Se connecter' }}
          </button>
        </p>
        <p class="muted" style="text-align:center;margin-bottom:0">
          Nouveau membre ? <a routerLink="/register"><b>S'inscrire maintenant</b></a>
        </p>
      </div>
    </main>
  </div>`
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  email = ''; password = ''; error = ''; loading = false;
  demos = [
    { label: 'Président', email: 'president@coophabitat.sn' },
    { label: 'Trésorier', email: 'tresorier@coophabitat.sn' },
    { label: 'Secrétaire', email: 'secretaire@coophabitat.sn' },
    { label: 'Membre à jour', email: 'coumbel.ka@coophabitat.sn' },
    { label: 'Membre en retard', email: 'abdoulaye.d@coophabitat.sn' }
  ];
  fillDemo(email: string) {
    this.email = email;
    this.password = 'ChangeMe123!';
    this.error = '';
  }
  submit() {
    if (!this.email || !this.password) { this.error = 'Email et mot de passe requis.'; return; }
    this.loading = true; this.error = '';
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/app/accueil']),
      error: e => { this.error = errMsg(e); this.loading = false; }
    });
  }
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
  <div class="auth">
    <aside class="auth-visual">
      <div>
        <a routerLink="/" class="brand-logo" style="margin-bottom:2rem">
          <span class="brand-mark">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2"><path d="M3 10.5L12 3l9 7.5"/><path d="M5 9.5V20h14V9.5"/><path d="M9.5 20v-6h5v6"/></svg>
          </span>
          <span>CoopHabitat <span style="color:#5eead4">Sénégal</span></span>
        </a>
        <h1>Rejoignez la coopérative en quelques instants.</h1>
        <p>Dès votre adhésion, vous recevez votre numéro d'adhérent officiel, votre calendrier de cotisations et l'accès en temps réel au fonds commun.</p>
        <ul class="auth-features">
          <li><span>1</span> Numéro d'adhérent unique (ADH-AAAA-NNN)</li>
          <li><span>2</span> Échéancier mensuel généré automatiquement</li>
          <li><span>3</span> Paiement simplifié via Wave / SendFlash</li>
          <li><span>4</span> Éligibilité transparente aux tirages au sort des lots</li>
        </ul>
        <div class="auth-bg-photo">
          <img src="/images/hero-residence.jpg" alt="Projet résidentiel CoopHabitat Sénégal">
        </div>
      </div>
      <div style="font-size:.82rem;color:#94a3b8">CoopHabitat Sénégal · Adhésion numérique sécurisée</div>
    </aside>

    <main class="auth-panel">
      <div class="box">
        <a routerLink="/" class="back-home">← Retour à l'accueil</a>
        <div class="brand">Adhésion membre</div>
        <p class="muted" style="margin-top:0">Complétez vos informations pour créer votre espace adhérent.</p>

        <label>Nom complet</label><input [(ngModel)]="f.nom" placeholder="Ex : Aminata Ka">
        <label>Email</label><input type="email" [(ngModel)]="f.email" placeholder="aminata@exemple.sn">
        <label>Téléphone</label><input [(ngModel)]="f.telephone" placeholder="+221 77 000 00 00">
        <label>Type de membre</label>
        <select [(ngModel)]="f.typeMembre">
          <option value="FONDATEUR">Fondateur</option>
          <option value="ORDINAIRE">Ordinaire</option>
          <option value="BENEFICIAIRE">Bénéficiaire</option>
        </select>
        <label>Mot de passe (8 caractères minimum)</label>
        <input type="password" [(ngModel)]="f.motDePasse" (keyup.enter)="submit()">

        <p class="muted" style="font-size:.78rem">Votre compte est créé avec le rôle <b>Membre</b>. Les rôles du bureau sont attribués exclusivement par le Président.</p>
        @if (error) { <div class="msg err">{{ error }}</div> }
        @if (ok) {
          <div class="msg ok">
            <span>Inscription réussie ! Votre numéro d'adhérent : <b>{{ ok }}</b>.</span>
            <a routerLink="/login" class="btn primary">Se connecter →</a>
          </div>
        }
        <p style="margin-top:1rem">
          <button class="btn primary" style="width:100%;padding:.7rem" [disabled]="loading" (click)="submit()">
            {{ loading ? 'Création du compte...' : "Valider l'inscription" }}
          </button>
        </p>
        <p class="muted" style="text-align:center;margin-bottom:0">Déjà inscrit ? <a routerLink="/login"><b>Se connecter</b></a></p>
      </div>
    </main>
  </div>`
})
export class RegisterComponent {
  private auth = inject(AuthService);
  f: any = { nom: '', email: '', telephone: '', typeMembre: 'ORDINAIRE', motDePasse: '' };
  error = ''; ok = ''; loading = false;
  submit() {
    this.loading = true; this.error = ''; this.ok = '';
    this.auth.register(this.f).subscribe({
      next: m => { this.ok = m.numeroAdherent; this.loading = false; },
      error: e => { this.error = errMsg(e); this.loading = false; }
    });
  }
}
