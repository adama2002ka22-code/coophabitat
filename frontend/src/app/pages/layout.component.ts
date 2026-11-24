import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subscription, catchError, of, switchMap, timer } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ROLE_LABEL } from '../core/util';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
  <div class="shell">
    <header class="topbar">
      <a routerLink="/" class="brand-logo" style="font-size:1.05rem">
        <span class="brand-mark" style="width:32px;height:32px;border-radius:9px">
          <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2"><path d="M3 10.5L12 3l9 7.5"/><path d="M5 9.5V20h14V9.5"/><path d="M9.5 20v-6h5v6"/></svg>
        </span>
        <span>CoopHabitat</span>
      </a>

      <div class="topbar-who">
        <div class="user-meta">
          <b>{{ auth.user?.nom }}</b>
          <span class="role-chip">{{ roleLabel }}</span>
          <span style="opacity:.75;margin-left:.35rem;font-size:.74rem">{{ auth.user?.numeroAdherent }}</span>
        </div>
        <button class="logout-btn" (click)="auth.logout()">Déconnexion</button>
      </div>
    </header>

    <nav class="nav" aria-label="Menu principal">
      <a routerLink="accueil" routerLinkActive="active">Accueil</a>
      <a routerLink="mon-espace" routerLinkActive="active">Mon espace</a>
      @if (auth.isBureau) { <a routerLink="membres" routerLinkActive="active">Membres</a> }
      <a routerLink="lots" routerLinkActive="active">Lots</a>
      <a routerLink="comptabilite" routerLinkActive="active">Comptabilité</a>
      <a routerLink="assemblees" routerLinkActive="active">Assemblées</a>
      <a routerLink="rapports" routerLinkActive="active">Rapports</a>
      <a routerLink="notifications" routerLinkActive="active">
        Notifications @if (unread > 0) { <span class="badge-n">{{ unread }}</span> }
      </a>
    </nav>

    <main class="content"><router-outlet /></main>

    <!-- Barre de navigation basse mobile (360-375px PWA) -->
    <nav class="mobile-nav" aria-label="Navigation mobile">
      <a routerLink="accueil" routerLinkActive="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10.5L12 3l9 7.5"/><path d="M5 9.5V20h14V9.5"/></svg>
        <span>Accueil</span>
      </a>
      <a routerLink="mon-espace" routerLinkActive="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>
        <span>Cotisations</span>
      </a>
      <a routerLink="lots" routerLinkActive="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
        <span>Lots</span>
      </a>
      <a routerLink="comptabilite" routerLinkActive="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
        <span>Finances</span>
      </a>
      <a routerLink="notifications" routerLinkActive="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <span>Alertes @if (unread > 0) { ({{ unread }}) }</span>
      </a>
    </nav>
  </div>`
})
export class LayoutComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);
  private api = inject(ApiService);
  unread = 0;
  private sub?: Subscription;
  get roleLabel() { return ROLE_LABEL[this.auth.user?.role] ?? ''; }
  ngOnInit() {
    this.sub = timer(0, 30000).pipe(
      switchMap(() => this.api.get<any[]>('/notifications/me').pipe(catchError(() => of(null))))
    ).subscribe(n => { if (n) this.unread = n.filter(x => !x.lu).length; });
  }
  ngOnDestroy() { this.sub?.unsubscribe(); }
}
