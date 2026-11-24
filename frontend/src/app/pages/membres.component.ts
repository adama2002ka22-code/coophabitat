import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ROLE_LABEL, errMsg } from '../core/util';

@Component({
  selector: 'app-membres',
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe],
  template: `
  <div class="dash-hero estate anim-in">
    <div class="row" style="margin-bottom:0">
      <div>
        <span class="role-chip" style="margin-bottom:.4rem">ANNUAIRE &amp; ADHÉSIONS</span>
        <h1>Gestion des membres</h1>
        <span class="muted">{{ list.length }} adhérent(s) enregistré(s) · Gestion des profils, rôles et statuts</span>
      </div>
      @if (canEdit) { <button class="l-btn l-btn-primary" (click)="form = !form">{{ form ? 'Fermer' : '+ Inscrire un membre' }}</button> }
    </div>
  </div>
  @if (msg) { <div class="msg ok">{{ msg }}</div> }
  @if (err) { <div class="msg err">{{ err }}</div> }

  @if (form && canEdit) {
    <div class="card"><h3>Nouveau membre</h3>
      <div class="form-grid">
        <div><label>Nom complet</label><input [(ngModel)]="n.nom" placeholder="Ex : Cheikh Diop"></div>
        <div><label>Email</label><input type="email" [(ngModel)]="n.email" placeholder="cheikh@coophabitat.sn"></div>
        <div><label>Téléphone</label><input [(ngModel)]="n.telephone" placeholder="+221 77 000 00 00"></div>
        <div><label>Type</label><select [(ngModel)]="n.typeMembre"><option value="FONDATEUR">Fondateur</option><option value="ORDINAIRE">Ordinaire</option><option value="BENEFICIAIRE">Bénéficiaire</option></select></div>
        <div><label>Rôle</label><select [(ngModel)]="n.role">
          <option value="MEMBRE">Membre</option>
          @if (auth.hasRole('PRESIDENT')) { <option value="PRESIDENT">Président</option><option value="TRESORIER">Trésorier</option><option value="SECRETAIRE">Secrétaire</option> }
        </select></div>
        <div><label>Mot de passe initial</label><input type="password" [(ngModel)]="n.motDePasse"></div>
      </div>
      <p style="margin-bottom:0"><button class="btn primary" (click)="creer()">Enregistrer le membre</button></p>
    </div>
  }

  <div class="card">
    <div class="filter-bar">
      <input [(ngModel)]="query" placeholder="Rechercher nom, n° adhérent, email...">
      <div class="tabs" style="margin:0">
        <button [class.on]="filtre === 'TOUS'" (click)="filtre = 'TOUS'">Tous ({{ list.length }})</button>
        <button [class.on]="filtre === 'A_JOUR'" (click)="filtre = 'A_JOUR'">À jour</button>
        <button [class.on]="filtre === 'EN_RETARD'" (click)="filtre = 'EN_RETARD'">En retard</button>
        <button [class.on]="filtre === 'SUSPENDU'" (click)="filtre = 'SUSPENDU'">Suspendus</button>
      </div>
    </div>

    <div class="tw"><table>
      <thead><tr><th>N° adhérent</th><th>Membre</th><th>Type</th><th>Rôle</th><th>Statut</th><th>Cotisation</th><th>Adhésion</th><th>Actions</th></tr></thead><tbody>
      @for (m of filtres; track m.id) {
        <tr>
          <td><b>{{ m.numeroAdherent }}</b></td>
          <td><b>{{ m.nom }}</b><br><span class="muted">{{ m.email }} · {{ m.telephone }}</span></td>
          <td>{{ m.typeMembre }}</td>
          <td><span class="badge">{{ roles[m.role] }}</span></td>
          <td><span class="badge" [class.ok]="m.statut === 'ACTIF'" [class.err]="m.statut === 'SUSPENDU'">{{ m.statut }}</span></td>
          <td><span class="badge" [class.ok]="m.statutCotisation === 'A_JOUR'" [class.err]="m.statutCotisation === 'EN_RETARD'">{{ m.statutCotisation === 'A_JOUR' ? 'À jour' : 'En retard' }}</span></td>
          <td>{{ m.dateAdhesion | date:'dd/MM/yyyy' }}</td>
          <td class="btns">
            <button class="btn" (click)="situation(m)">Situation</button>
            @if (canEdit) {
              <button class="btn" [class.danger]="m.statut === 'ACTIF'" (click)="basculer(m)">
                {{ m.statut === 'ACTIF' ? 'Suspendre' : 'Réactiver' }}
              </button>
            }
          </td>
        </tr>
      } @empty {
        <tr><td colspan="8" class="muted" style="text-align:center;padding:1.2rem">Aucun membre ne correspond à ce filtre.</td></tr>
      }
    </tbody></table></div>
  </div>

  @if (sit) {
    <div class="card">
      <div class="row"><h3 style="margin:0">Situation financière de {{ sit.nom }} ({{ sit.numeroAdherent }})</h3>
        <button class="btn" (click)="sit = null">Fermer</button></div>
      <div class="grid" style="margin-bottom:0">
        <div class="kpi err"><span>Solde restant dû</span><b>{{ sit.soldeRestantDu | number:'1.0-0' }} FCFA</b></div>
        <div class="kpi warn"><span>Pénalités</span><b>{{ sit.penalitesTotales | number:'1.0-0' }} FCFA</b></div>
        <div class="kpi ok"><span>Total payé</span><b>{{ sit.totalPaye | number:'1.0-0' }} FCFA</b></div>
      </div>
    </div>
  }`
})
export class MembresComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);
  list: any[] = []; sit: any = null; form = false; msg = ''; err = '';
  query = ''; filtre: 'TOUS' | 'A_JOUR' | 'EN_RETARD' | 'SUSPENDU' = 'TOUS';
  roles = ROLE_LABEL;
  n: any = { nom: '', email: '', telephone: '', typeMembre: 'ORDINAIRE', role: 'MEMBRE', motDePasse: '' };
  get canEdit() { return this.auth.hasRole('PRESIDENT', 'SECRETAIRE'); }
  get filtres() {
    const q = this.query.trim().toLowerCase();
    return this.list.filter(m => {
      if (this.filtre === 'A_JOUR' && m.statutCotisation !== 'A_JOUR') return false;
      if (this.filtre === 'EN_RETARD' && m.statutCotisation !== 'EN_RETARD') return false;
      if (this.filtre === 'SUSPENDU' && m.statut !== 'SUSPENDU') return false;
      if (!q) return true;
      return (m.nom + ' ' + m.email + ' ' + m.numeroAdherent).toLowerCase().includes(q);
    });
  }
  ngOnInit() { this.charger(); }
  charger() { this.api.get<any[]>('/membres').subscribe({ next: l => this.list = l, error: e => this.err = errMsg(e) }); }
  creer() {
    this.msg = ''; this.err = '';
    this.api.post('/membres', this.n).subscribe({
      next: m => {
        this.msg = `Membre créé avec succès : ${m.numeroAdherent}`; this.form = false; this.charger();
        this.n = { nom: '', email: '', telephone: '', typeMembre: 'ORDINAIRE', role: 'MEMBRE', motDePasse: '' };
      },
      error: e => this.err = errMsg(e)
    });
  }
  basculer(m: any) {
    const nextStatut = m.statut === 'ACTIF' ? 'SUSPENDU' : 'ACTIF';
    const action = nextStatut === 'SUSPENDU' ? `suspendre ${m.nom} (son accès sera immédiatement bloqué)` : `réactiver ${m.nom}`;
    if (!confirm(`Voulez-vous vraiment ${action} ?`)) return;
    this.api.patch(`/membres/${m.id}/statut`, { statut: nextStatut })
      .subscribe({ next: () => { this.msg = `Statut de ${m.nom} mis à jour : ${nextStatut}.`; this.charger(); }, error: e => this.err = errMsg(e) });
  }
  situation(m: any) { this.api.get(`/membres/${m.id}/situation`).subscribe({ next: s => this.sit = s, error: e => this.err = errMsg(e) }); }
}
