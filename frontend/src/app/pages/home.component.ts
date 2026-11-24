import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, catchError, forkJoin, of, switchMap, timer } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

interface ChartPoint { label: string; value: number; x: number; y: number; }

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DecimalPipe, DatePipe, FormsModule, RouterLink],
  template: `
  <div class="dash-hero anim-in">
    <div class="row" style="margin-bottom:0">
      <div>
        <span class="role-chip" style="margin-bottom:.45rem">{{ auth.user?.role }} · COOPHABITAT SÉNÉGAL</span>
        <h1>Tableau de bord</h1>
        <span class="muted">Bonjour <b>{{ auth.user?.nom }}</b> · Pilotage immobilier &amp; financier en temps réel</span>
      </div>
      @if (updated) { <span class="hero-photo-badge">● En direct · {{ updated | date:'HH:mm:ss' }}</span> }
    </div>
  </div>

  @if (gestion && dash) {
    <!-- Barre de contrôle compacte : menu déroulant de vue + période + recherche -->
    <div class="dash-toolbar anim-in">
      <div style="display:flex;flex-wrap:wrap;gap:.65rem;align-items:center">
        <label style="margin:0">Vue à afficher :</label>
        <select [(ngModel)]="vue">
          <option value="SYNTHESE">Vue synthétique (Graphiques + KPI)</option>
          <option value="COTISATIONS">Courbe &amp; Cotisations par période</option>
          <option value="ASSIDUITE">Classement des membres les plus assidus</option>
          <option value="SEQUESTRE">Séquestre &amp; Trésorerie</option>
          <option value="RETARDS">Membres en retard ({{ dash.membresEnRetard }})</option>
        </select>

        <label style="margin:0">Période :</label>
        <select [(ngModel)]="periode">
          <option value="SEMAINE">7 derniers jours (Semaine)</option>
          <option value="MOIS">30 derniers jours (Mois)</option>
          <option value="ANNEE">12 derniers mois (Année)</option>
        </select>
      </div>

      <input [(ngModel)]="search" placeholder="Rechercher par matricule (ADH-...) ou nom..." style="min-width:240px">
    </div>

    <!-- KPI essentiels -->
    <div class="grid anim-in">
      <div class="kpi ok"><span>Membres à jour</span><b>{{ dash.membresAJour }} / {{ dash.membresActifs }}</b></div>
      <div class="kpi err"><span>Membres en retard</span><b>{{ dash.membresEnRetard }}</b></div>
      <div class="kpi warn"><span>Trésorerie disponible</span><b>{{ dash.tresorerie.tresorerieDisponible | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi"><span>Cotisations ({{ periodeLabel }})</span><b>{{ totalPeriode | number:'1.0-0' }} FCFA</b></div>
    </div>

    <!-- GRAPHIQUES ANIMÉS (Courbe cotisations + Donut régularité) -->
    @if (vue === 'SYNTHESE' || vue === 'COTISATIONS') {
      <div class="charts-grid anim-in">
        <div class="card" style="margin-bottom:0">
          <div class="row" style="margin-bottom:.4rem">
            <h3 style="margin:0">Évolution des cotisations ({{ periodeLabel }})</h3>
            <span class="badge ok">{{ paiementsFiltres.length }} versement(s)</span>
          </div>
          <svg viewBox="0 0 420 175" width="100%" style="display:block;overflow:visible" role="img" aria-label="Courbe des cotisations">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#14b8a6" stop-opacity="0.35"/>
                <stop offset="100%" stop-color="#14b8a6" stop-opacity="0.02"/>
              </linearGradient>
            </defs>
            <line x1="20" y1="30" x2="400" y2="30" stroke="#e2e8f0" stroke-dasharray="3 3"/>
            <line x1="20" y1="82" x2="400" y2="82" stroke="#e2e8f0" stroke-dasharray="3 3"/>
            <line x1="20" y1="135" x2="400" y2="135" stroke="#cbd5e1"/>
            <path [attr.d]="areaPath" fill="url(#areaGrad)"/>
            <path class="chart-line" [attr.d]="linePath" fill="none" stroke="#0f766e" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            @for (pt of chartPoints; track pt.label) {
              <circle [attr.cx]="pt.x" [attr.cy]="pt.y" r="4.5" fill="#0f766e" stroke="#fff" stroke-width="2"/>
              <text [attr.x]="pt.x" y="156" text-anchor="middle" font-size="10" fill="#64748b">{{ pt.label }}</text>
            }
          </svg>
          <p class="muted" style="margin:.4rem 0 0;font-size:.8rem">
            Total encaissé sur la période : <b>{{ totalPeriode | number:'1.0-0' }} FCFA</b>
          </p>
        </div>

        <div class="card" style="margin-bottom:0">
          <h3 style="margin-bottom:.8rem">Répartition des membres</h3>
          <div class="donut-wrap">
            <svg width="132" height="132" viewBox="0 0 132 132" role="img" aria-label="Taux de membres à jour">
              <circle cx="66" cy="66" r="50" fill="none" stroke="#fee2e2" stroke-width="16"/>
              <circle cx="66" cy="66" r="50" fill="none" stroke="#10b981" stroke-width="16"
                      stroke-linecap="round"
                      [attr.stroke-dasharray]="donutCircumference"
                      [attr.stroke-dashoffset]="donutOffset"
                      transform="rotate(-90 66 66)"
                      style="transition: stroke-dashoffset .6s ease"/>
              <text x="66" y="64" text-anchor="middle" font-size="22" font-weight="800" fill="#0f172a">{{ tauxAJour }}%</text>
              <text x="66" y="81" text-anchor="middle" font-size="10" fill="#64748b">à jour</text>
            </svg>
            <div class="donut-legend">
              <div><span class="dot" style="background:#10b981"></span><b>{{ dash.membresAJour }}</b> membres à jour</div>
              <div><span class="dot" style="background:#ef4444"></span><b>{{ dash.membresEnRetard }}</b> membre(s) en retard</div>
              <div><span class="dot" style="background:#f59e0b"></span>Impayés : <b>{{ dash.totalImpayes | number:'1.0-0' }} FCFA</b></div>
              <a routerLink="/app/comptabilite" class="btn" style="margin-top:.3rem">Ouvrir la comptabilité →</a>
            </div>
          </div>
        </div>
      </div>
    }

    <!-- LISTE FILTRÉE DES COTISATIONS DE LA PÉRIODE -->
    @if (vue === 'COTISATIONS') {
      <div class="card anim-in">
        <div class="row">
          <h3 style="margin:0">Versements de la période ({{ periodeLabel }})</h3>
          <span class="muted">{{ paiementsFiltres.length }} résultat(s)</span>
        </div>
        @if (paiementsFiltres.length === 0) {
          <p class="muted" style="margin:0">Aucun paiement trouvé pour cette période / recherche.</p>
        } @else {
          <div class="tw"><table>
            <thead><tr><th>Reçu</th><th>Date</th><th>Adhérent</th><th>Canal</th><th>Montant</th></tr></thead><tbody>
            @for (p of paiementsFiltres; track p.id) {
              <tr>
                <td><b>{{ p.recuNumero }}</b></td>
                <td>{{ p.datePaiement | date:'dd/MM/yyyy HH:mm' }}</td>
                <td><b>{{ p.membreNom }}</b> <span class="muted">({{ p.membreNumero }})</span></td>
                <td><span class="badge">{{ p.canal }}</span></td>
                <td><b style="color:var(--ok)">{{ p.montant | number:'1.0-0' }} FCFA</b></td>
              </tr>
            }
          </tbody></table></div>
        }
      </div>
    }

    <!-- CLASSEMENT DES MEMBRES LES PLUS ASSIDUS -->
    @if (vue === 'ASSIDUITE') {
      <div class="card anim-in">
        <div class="row">
          <div>
            <h3 style="margin:0">Classement des membres les plus assidus</h3>
            <span class="muted">Classés par régularité (à jour en priorité), total cotisé et nombre de versements</span>
          </div>
          <span class="badge ok">Top {{ classementAssiduite.length }} adhérent(s)</span>
        </div>
        @if (classementAssiduite.length === 0) {
          <p class="muted" style="margin:0">Aucun membre trouvé pour cette recherche.</p>
        } @else {
          <div class="tw"><table>
            <thead><tr><th>Rang</th><th>Matricule</th><th>Membre</th><th>Versements</th><th>Total cotisé</th><th>Assiduité</th></tr></thead><tbody>
            @for (m of classementAssiduite; track m.id; let idx = $index) {
              <tr>
                <td><b>#{{ idx + 1 }}</b></td>
                <td><b>{{ m.numeroAdherent }}</b></td>
                <td><b>{{ m.nom }}</b> <span class="muted">· {{ m.typeMembre }}</span></td>
                <td>{{ m.nbVersements }}</td>
                <td><b style="color:var(--ok)">{{ m.totalCotise | number:'1.0-0' }} FCFA</b></td>
                <td>
                  <span class="badge" [class.ok]="m.statutCotisation === 'A_JOUR'" [class.err]="m.statutCotisation !== 'A_JOUR'">
                    {{ m.statutCotisation === 'A_JOUR' ? (m.nbVersements >= 1 ? 'Exemplaire (À jour)' : 'À jour') : 'En retard' }}
                  </span>
                </td>
              </tr>
            }
          </tbody></table></div>
        }
      </div>
    }

    <!-- RETARDATAIRES (compact dans Synthèse ou complet dans Retards) -->
    @if (vue === 'RETARDS' || (vue === 'SYNTHESE' && search.trim())) {
      <div class="card anim-in">
        <div class="row">
          <h3 style="margin:0">Membres en retard de cotisation</h3>
          <a routerLink="/app/comptabilite" class="btn">Envoyer rappels J-3 →</a>
        </div>
        @if (retardatairesFiltres.length === 0) {
          <p class="muted" style="margin:0">Aucun membre en retard ne correspond à la recherche.</p>
        } @else {
          <div class="tw"><table><thead><tr><th>Matricule</th><th>Nom</th><th>Téléphone</th><th>Solde dû</th><th>Retard max</th></tr></thead><tbody>
            @for (r of retardatairesFiltres; track r.membreId) {
              <tr>
                <td><b>{{ r.numeroAdherent }}</b></td>
                <td>{{ r.nom }}</td>
                <td>{{ r.telephone }}</td>
                <td><b style="color:var(--err)">{{ r.soldeRestantDu | number:'1.0-0' }} FCFA</b></td>
                <td><span class="badge err">{{ r.joursRetardMax }} j</span></td>
              </tr>
            }
          </tbody></table></div>
        }
      </div>
    }
  }

  <!-- VUE MEMBRE / SECRÉTAIRE -->
  @if (!gestion && me) {
    <div class="grid anim-in">
      <div class="kpi" [class.ok]="me.statutCotisation === 'A_JOUR'" [class.err]="me.statutCotisation === 'EN_RETARD'">
        <span>Ma situation</span><b>{{ me.statutCotisation === 'A_JOUR' ? 'À jour' : 'En retard' }}</b>
      </div>
      <div class="kpi err"><span>Solde restant dû</span><b>{{ me.soldeRestantDu | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi"><span>Prochaine échéance</span><b>{{ me.prochaineEcheance ? (me.prochaineEcheance | date:'dd/MM/yyyy') : '-' }}</b></div>
    </div>
    <div class="btns anim-in" style="margin-bottom:1.1rem">
      <a class="btn primary" routerLink="/app/mon-espace">Payer par Wave / SendFlash →</a>
      <a class="btn" routerLink="/app/lots">Consulter les lots disponibles</a>
      <a class="btn" routerLink="/app/assemblees">Voir les Assemblées Générales</a>
    </div>
  }

  <!-- SÉQUESTRE (toujours visible pour les membres, ou via le menu déroulant pour le bureau) -->
  @if (seq && (!gestion || vue === 'SYNTHESE' || vue === 'SEQUESTRE')) {
    <div class="card anim-in">
      <div class="row" style="margin-bottom:.5rem">
        <h3 style="margin:0">Séquestre du fonds commun</h3>
        <span class="badge ok">Transparence · Visible par tous</span>
      </div>
      <div class="grid" style="margin-bottom:.5rem">
        <div class="kpi warn"><span>Trésorerie disponible</span><b>{{ seq.tresorerieDisponible | number:'1.0-0' }} FCFA</b></div>
        <div class="kpi ok"><span>Total encaissé</span><b>{{ seq.totalEncaisse | number:'1.0-0' }} FCFA</b></div>
        <div class="kpi err"><span>Dépenses réelles</span><b>{{ seq.totalDepenses | number:'1.0-0' }} FCFA</b></div>
      </div>
      @if (seq.totalEncaisse > 0) {
        <div class="progress-wrap">
          <div class="progress-label">
            <span>Utilisation des fonds ({{ ratioDepense }} % dépensés)</span>
            <span>{{ 100 - ratioDepense }} % conservés en séquestre</span>
          </div>
          <div class="progress-track">
            <div class="progress-fill warn" [style.width.%]="ratioDepense"></div>
          </div>
        </div>
      }
    </div>
  }

  @if (offlineNote) { <div class="msg info">Mode hors ligne : affichage des dernières données disponibles.</div> }`
})
export class HomeComponent implements OnInit, OnDestroy {
  private api = inject(ApiService);
  auth = inject(AuthService);
  seq: any = null; dash: any = null; me: any = null; paiements: any[] = []; membres: any[] = [];
  updated: Date | null = null; offlineNote = false;
  vue: 'SYNTHESE' | 'COTISATIONS' | 'ASSIDUITE' | 'SEQUESTRE' | 'RETARDS' = 'SYNTHESE';
  periode: 'SEMAINE' | 'MOIS' | 'ANNEE' = 'MOIS';
  search = '';
  readonly donutCircumference = 2 * Math.PI * 50;
  private sub?: Subscription;

  get gestion() { return this.auth.hasRole('PRESIDENT', 'TRESORIER'); }
  get periodeLabel() {
    return this.periode === 'SEMAINE' ? '7 jours' : this.periode === 'MOIS' ? '30 jours' : '12 mois';
  }
  get tauxAJour() {
    if (!this.dash || !this.dash.membresActifs) return 100;
    return Math.round((this.dash.membresAJour / this.dash.membresActifs) * 100);
  }
  get donutOffset() {
    return this.donutCircumference * (1 - this.tauxAJour / 100);
  }
  get ratioDepense() {
    if (!this.seq || !this.seq.totalEncaisse) return 0;
    return Math.min(100, Math.round((this.seq.totalDepenses / this.seq.totalEncaisse) * 100));
  }

  get paiementsFiltres() {
    const now = Date.now();
    const days = this.periode === 'SEMAINE' ? 7 : this.periode === 'MOIS' ? 30 : 365;
    const cutoff = now - days * 86400000;
    const q = this.search.trim().toLowerCase();
    return this.paiements.filter(p => {
      const t = new Date(p.datePaiement).getTime();
      if (isFinite(t) && t < cutoff) return false;
      if (!q) return true;
      return `${p.membreNom ?? ''} ${p.membreNumero ?? ''} ${p.recuNumero ?? ''}`.toLowerCase().includes(q);
    });
  }

  get totalPeriode() {
    return this.paiementsFiltres.reduce((acc, p) => acc + Number(p.montant || 0), 0);
  }

  get retardatairesFiltres() {
    const list: any[] = this.dash?.retardataires ?? [];
    const q = this.search.trim().toLowerCase();
    if (!q) return list;
    return list.filter(r => `${r.nom} ${r.numeroAdherent} ${r.telephone ?? ''}`.toLowerCase().includes(q));
  }

  get classementAssiduite() {
    const q = this.search.trim().toLowerCase();
    const parMembre = new Map<number, { count: number; total: number }>();
    for (const p of this.paiements) {
      const id = Number(p.membreId);
      const cur = parMembre.get(id) ?? { count: 0, total: 0 };
      cur.count += 1;
      cur.total += Number(p.montant || 0);
      parMembre.set(id, cur);
    }
    return this.membres
      .filter(m => m.statut === 'ACTIF')
      .filter(m => !q || `${m.nom} ${m.numeroAdherent} ${m.email ?? ''}`.toLowerCase().includes(q))
      .map(m => {
        const stats = parMembre.get(Number(m.id)) ?? { count: 0, total: 0 };
        return { ...m, nbVersements: stats.count, totalCotise: stats.total };
      })
      .sort((a, b) => {
        const aOk = a.statutCotisation === 'A_JOUR' ? 1 : 0;
        const bOk = b.statutCotisation === 'A_JOUR' ? 1 : 0;
        if (bOk !== aOk) return bOk - aOk;
        if (b.totalCotise !== a.totalCotise) return b.totalCotise - a.totalCotise;
        return b.nbVersements - a.nbVersements;
      });
  }

  get chartPoints(): ChartPoint[] {
    const buckets: { label: string; sum: number }[] = [];
    const now = new Date();
    if (this.periode === 'SEMAINE') {
      for (let i = 6; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i);
        const key = d.toISOString().slice(0, 10);
        const sum = this.paiementsFiltres.filter(p => String(p.datePaiement).startsWith(key))
          .reduce((a, p) => a + Number(p.montant || 0), 0);
        buckets.push({ label: `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}`, sum });
      }
    } else if (this.periode === 'MOIS') {
      for (let w = 3; w >= 0; w--) {
        const end = now.getTime() - w * 7 * 86400000;
        const start = end - 7 * 86400000;
        const sum = this.paiementsFiltres.filter(p => {
          const t = new Date(p.datePaiement).getTime();
          return t >= start && t <= end;
        }).reduce((a, p) => a + Number(p.montant || 0), 0);
        buckets.push({ label: `Sem. ${4 - w}`, sum });
      }
    } else {
      const moisNoms = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'];
      for (let m = 5; m >= 0; m--) {
        const d = new Date(now.getFullYear(), now.getMonth() - m, 1);
        const ym = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
        const sum = this.paiementsFiltres.filter(p => String(p.datePaiement).startsWith(ym))
          .reduce((a, p) => a + Number(p.montant || 0), 0);
        buckets.push({ label: moisNoms[d.getMonth()], sum });
      }
    }
    const max = Math.max(1, ...buckets.map(b => b.sum));
    const n = Math.max(1, buckets.length - 1);
    return buckets.map((b, idx) => ({
      label: b.label,
      value: b.sum,
      x: Math.round(35 + (idx / n) * 350),
      y: Math.round(130 - (b.sum / max) * 95)
    }));
  }

  get linePath(): string {
    const pts = this.chartPoints;
    if (!pts.length) return '';
    return pts.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  }

  get areaPath(): string {
    const pts = this.chartPoints;
    if (!pts.length) return '';
    return `${this.linePath} L ${pts[pts.length - 1].x} 135 L ${pts[0].x} 135 Z`;
  }

  ngOnInit() {
    this.sub = timer(0, 10000).pipe(switchMap(() => forkJoin({
      seq: this.api.get('/tresorerie/sequestre').pipe(catchError(() => of(null))),
      dash: this.gestion ? this.api.get('/dashboard').pipe(catchError(() => of(null))) : of(null),
      pays: this.gestion ? this.api.get<any[]>('/paiements').pipe(catchError(() => of([]))) : of([]),
      membres: this.gestion ? this.api.get<any[]>('/membres').pipe(catchError(() => of([]))) : of([]),
      me: !this.gestion ? this.api.get('/cotisations/me').pipe(catchError(() => of(null))) : of(null)
    }))).subscribe(r => {
      this.seq = r.seq ?? this.seq;
      this.dash = r.dash ?? this.dash;
      this.paiements = r.pays ?? this.paiements;
      this.membres = r.membres ?? this.membres;
      this.me = r.me ?? this.me;
      this.offlineNote = !r.seq;
      if (r.seq) this.updated = new Date();
    });
  }
  ngOnDestroy() { this.sub?.unsubscribe(); }
}
