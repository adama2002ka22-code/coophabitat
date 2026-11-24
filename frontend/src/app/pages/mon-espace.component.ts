import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

@Component({
  selector: 'app-mon-espace',
  standalone: true,
  imports: [DecimalPipe, DatePipe, FormsModule, RouterLink],
  template: `
  <div class="dash-hero villa anim-in">
    <div class="row" style="margin-bottom:0">
      <div>
        <span class="role-chip" style="margin-bottom:.4rem">ESPACE ADHÉRENT · COOPHABITAT</span>
        <h1>Mon espace membre</h1>
        @if (s) { <span class="muted">N° adhérent <b>{{ s.numeroAdherent }}</b> · Rappel automatique SMS / e-mail 3 jours avant chaque échéance</span> }
      </div>
    </div>
  </div>

  @if (s) {
    <div class="grid">
      <div class="kpi" [class.ok]="s.statutCotisation === 'A_JOUR'" [class.err]="s.statutCotisation === 'EN_RETARD'">
        <span>Statut cotisation</span><b>{{ s.statutCotisation === 'A_JOUR' ? 'À jour' : 'En retard' }}</b>
      </div>
      <div class="kpi err"><span>Solde restant dû</span><b>{{ s.soldeRestantDu | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi warn"><span>Pénalités de retard</span><b>{{ s.penalitesTotales | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi ok"><span>Total payé</span><b>{{ s.totalPaye | number:'1.0-0' }} FCFA</b></div>
    </div>

    <div class="card">
      <h3>Payer ma cotisation par Mobile Money (Wave / SendFlash · Sandbox)</h3>
      <div class="op-grid">
        <button type="button" class="op-card wave" [class.on]="pay.operateur === 'WAVE'" (click)="pay.operateur = 'WAVE'">
          <b>Wave</b>
          <span>Paiement instantané · Reçu PDF auto</span>
        </button>
        <button type="button" class="op-card sf" [class.on]="pay.operateur === 'SENDFLASH'" (click)="pay.operateur = 'SENDFLASH'">
          <b>SendFlash</b>
          <span>Paiement mobile · Reçu PDF auto</span>
        </button>
      </div>
      <div class="form-grid">
        <div><label>Numéro Mobile Money</label><input [(ngModel)]="pay.telephone" placeholder="77 123 45 67"></div>
        <div><label>Montant à régler (FCFA)</label><input type="number" [(ngModel)]="pay.montant" min="1"></div>
      </div>
      <p class="muted" style="font-size:.8rem">Sandbox : tout numéro valide est accepté, sauf ceux se terminant par <b>0000</b> (échec simulé pour la démo).</p>
      <div class="btns">
        <button class="btn primary" [disabled]="busy || !online || !pay.montant" (click)="payer()">
          {{ busy ? 'Traitement en cours...' : 'Confirmer le paiement ' + pay.operateur }}
        </button>
        @if (!online) { <span class="badge warn">Indisponible hors ligne</span> }
      </div>
      @if (msg) {
        <div class="msg ok">
          <span>{{ msg }}</span>
          @if (lastPaiement) { <button class="btn primary" (click)="recu(lastPaiement)">Télécharger mon reçu PDF</button> }
        </div>
      }
      @if (err) { <div class="msg err">{{ err }}</div> }
    </div>

    <div class="card"><h3>Calendrier des échéances</h3><div class="tw"><table>
      <thead><tr><th>Échéance</th><th>Dû</th><th>Pénalité</th><th>Payé</th><th>Reste</th><th>Statut</th></tr></thead><tbody>
      @for (e of s.calendrier; track e.id) {
        <tr>
          <td><b>{{ e.dateEcheance | date:'dd/MM/yyyy' }}</b></td>
          <td>{{ e.montantDu | number:'1.0-0' }} FCFA</td>
          <td>{{ e.penalite | number:'1.0-0' }} FCFA</td>
          <td>{{ e.montantPaye | number:'1.0-0' }} FCFA</td>
          <td><b>{{ e.solde | number:'1.0-0' }} FCFA</b></td>
          <td><span class="badge" [class.ok]="e.statut === 'PAYEE'" [class.err]="e.statut === 'EN_RETARD'" [class.warn]="e.statut === 'PARTIELLE'">{{ statuts[e.statut] }}</span></td>
        </tr>
      }
    </tbody></table></div></div>

    <div class="card"><h3>Historique complet des paiements</h3>
      @if (s.paiements.length === 0) { <p class="muted">Aucun paiement enregistré pour le moment.</p> } @else {
      <div class="tw"><table><thead><tr><th>Reçu</th><th>Date</th><th>Canal</th><th>Référence</th><th>Montant</th><th>Justificatif</th></tr></thead><tbody>
        @for (p of s.paiements; track p.id) {
          <tr>
            <td><b>{{ p.recuNumero }}</b></td>
            <td>{{ p.datePaiement | date:'dd/MM/yyyy HH:mm' }}</td>
            <td><span class="badge">{{ p.canal }}</span></td>
            <td>{{ p.reference }}</td>
            <td><b>{{ p.montant | number:'1.0-0' }} FCFA</b></td>
            <td><button class="btn" (click)="recu(p)">Reçu PDF</button></td>
          </tr>
        }
      </tbody></table></div>}
    </div>

    <div class="card"><h3>Documents officiels</h3>
      @for (d of docs; track d.url) {
        <div class="row" style="border-bottom:1px solid var(--line);padding:.5rem 0;margin-bottom:0">
          <div><b>{{ d.titre }}</b><br><span class="muted">{{ d.date | date:'dd/MM/yyyy' }}</span></div>
          @if (d.type === 'COMPTE_RENDU') { <a class="btn" routerLink="/app/assemblees">Consulter →</a> }
          @else { <button class="btn primary" (click)="telecharger(d)">Télécharger PDF</button> }
        </div>
      } @empty { <p class="muted">Aucun document officiel disponible.</p> }
    </div>
  } @else { <p class="muted">Chargement de votre espace membre...</p> }`
})
export class MonEspaceComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  s: any = null; docs: any[] = []; msg = ''; err = ''; busy = false; lastPaiement: any = null;
  pay: any = { operateur: 'WAVE', telephone: '', montant: null };
  statuts: any = { PAYEE: 'Payée', EN_RETARD: 'En retard', PARTIELLE: 'Partielle', A_VENIR: 'À venir' };
  get online() { return navigator.onLine; }

  ngOnInit() { this.charger(); this.api.get<any[]>('/documents').subscribe(d => this.docs = d); }

  charger() {
    this.api.get('/cotisations/me').subscribe(s => {
      this.s = s;
      this.pay.telephone = this.pay.telephone || this.auth.user?.telephone || '';
      const prochain = s.calendrier.find((e: any) => e.solde > 0);
      this.pay.montant = s.soldeRestantDu > 0 ? s.soldeRestantDu : (prochain ? prochain.solde : null);
    });
  }
  payer() {
    this.msg = ''; this.err = ''; this.busy = true; this.lastPaiement = null;
    this.api.post('/paiements/mobile-money', { telephone: this.pay.telephone, montant: this.pay.montant, operateur: this.pay.operateur }).subscribe({
      next: p => {
        this.busy = false;
        this.lastPaiement = p;
        this.msg = `Paiement accepté ! Reçu officiel ${p.recuNumero} généré automatiquement par JasperReports.`;
        this.charger();
      },
      error: e => { this.busy = false; this.err = errMsg(e); }
    });
  }
  recu(p: any) { this.api.download(`/paiements/${p.id}/recu`, `${p.recuNumero}.pdf`, e => this.err = errMsg(e)); }
  telecharger(d: any) { this.api.download(d.url, `${d.titre.replace(/[^a-zA-Z0-9-]+/g, '_')}.pdf`, e => this.err = errMsg(e)); }
}
