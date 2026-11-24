import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

@Component({
  selector: 'app-assemblees',
  standalone: true,
  imports: [FormsModule, DatePipe],
  template: `
  <div class="dash-hero villa anim-in">
    <div class="row" style="margin-bottom:0">
      <div>
        <span class="role-chip" style="margin-bottom:.4rem">GOUVERNANCE NUMÉRIQUE</span>
        <h1>Assemblée générale numérique</h1>
        <span class="muted">Convocations automatiques, ordre du jour, votes des délibérations et comptes rendus publiés</span>
      </div>
      @if (canManage) { <button class="l-btn l-btn-primary" (click)="form = !form">{{ form ? 'Fermer' : '+ Convoquer une AG' }}</button> }
    </div>
  </div>
  @if (msg) { <div class="msg ok">{{ msg }}</div> }
  @if (err) { <div class="msg err">{{ err }}</div> }

  @if (form && canManage) {
    <div class="card"><h3>Convocation</h3><div class="form-grid">
      <div><label>Titre</label><input [(ngModel)]="n.titre"></div>
      <div><label>Date et heure</label><input type="datetime-local" [(ngModel)]="n.dateAssemblee"></div>
      <div><label>Lieu</label><input [(ngModel)]="n.lieu"></div></div>
      <label>Ordre du jour</label><textarea rows="4" [(ngModel)]="n.ordreDuJour"></textarea>
      <p><button class="btn primary" (click)="convoquer()">Publier la convocation (notifie tous les membres)</button></p></div>
  }

  @for (a of list; track a.id) {
    <div class="card">
      <div class="row"><h3 style="margin:0">{{ a.titre }}</h3>
        <span class="badge" [class.warn]="a.statut === 'CONVOQUEE'" [class.ok]="a.statut === 'TENUE'">{{ a.statut === 'CONVOQUEE' ? 'Convoquée' : 'Tenue' }}</span></div>
      <p class="muted">{{ a.dateAssemblee | date:'dd/MM/yyyy HH:mm' }} · {{ a.lieu }}</p>
      <b>Ordre du jour</b><pre class="ordre">{{ a.ordreDuJour }}</pre>

      <b>Délibérations</b>
      @if (a.deliberations.length === 0) { <p class="muted">Aucune délibération enregistrée.</p> }
      @for (d of a.deliberations; track d.id) {
        <p style="margin:.3rem 0"><b>{{ d.titre }}</b> : {{ d.resolution }}<br>
          <span class="muted">Pour {{ d.pour }} · Contre {{ d.contre }} · Abstentions {{ d.abstention }}</span>
          <span class="badge" [class.ok]="d.adoptee" [class.err]="!d.adoptee"> {{ d.adoptee ? 'Adoptée' : 'Rejetée' }}</span></p>
      }
      @if (a.compteRenduPublie) { <p><b>Compte rendu publié</b></p><pre class="ordre">{{ a.compteRendu }}</pre> }

      @if (canManage) {
        <p><button class="btn" (click)="ouvrir(a)">{{ edit?.id === a.id ? 'Fermer' : 'Gérer (délibération / compte rendu)' }}</button></p>
        @if (edit?.id === a.id) {
          <div class="form-grid">
            <div><label>Titre de la délibération</label><input [(ngModel)]="d.titre"></div>
            <div><label>Pour</label><input type="number" [(ngModel)]="d.pour"></div>
            <div><label>Contre</label><input type="number" [(ngModel)]="d.contre"></div>
            <div><label>Abstentions</label><input type="number" [(ngModel)]="d.abstention"></div></div>
          <label>Résolution</label><textarea rows="2" [(ngModel)]="d.resolution"></textarea>
          <p><button class="btn primary" (click)="deliberer(a)">Enregistrer la délibération</button></p>
          <label>Compte rendu</label><textarea rows="4" [(ngModel)]="cr"></textarea>
          <p class="btns"><button class="btn" (click)="compteRendu(a, false)">Enregistrer le brouillon</button>
            <button class="btn accent" (click)="compteRendu(a, true)">Publier le compte rendu</button></p>
        }
      }
    </div>
  } @empty { <p class="muted">Aucune assemblée.</p> }`
})
export class AssembleesComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  list: any[] = []; form = false; msg = ''; err = ''; edit: any = null; cr = '';
  n: any = { titre: '', dateAssemblee: '', lieu: '', ordreDuJour: '' };
  d: any = { titre: '', resolution: '', pour: 0, contre: 0, abstention: 0 };
  get canManage() { return this.auth.hasRole('PRESIDENT', 'SECRETAIRE'); }
  private fail = (e: any) => this.err = errMsg(e);
  ngOnInit() { this.charger(); }
  charger() { this.api.get<any[]>('/assemblees').subscribe({ next: l => this.list = l, error: this.fail }); }
  convoquer() {
    this.msg = ''; this.err = '';
    this.api.post('/assemblees', this.n).subscribe({ next: () => { this.msg = 'Convocation publiée et notifiée à tous les membres.'; this.form = false; this.n = { titre: '', dateAssemblee: '', lieu: '', ordreDuJour: '' }; this.charger(); }, error: this.fail });
  }
  ouvrir(a: any) { this.edit = this.edit?.id === a.id ? null : a; this.cr = a.compteRendu ?? ''; }
  deliberer(a: any) {
    this.msg = ''; this.err = '';
    this.api.post(`/assemblees/${a.id}/deliberations`, this.d).subscribe({ next: () => { this.msg = 'Délibération enregistrée.'; this.d = { titre: '', resolution: '', pour: 0, contre: 0, abstention: 0 }; this.charger(); }, error: this.fail });
  }
  compteRendu(a: any, publier: boolean) {
    this.msg = ''; this.err = '';
    this.api.put(`/assemblees/${a.id}/compte-rendu`, { compteRendu: this.cr, publier }).subscribe({ next: () => { this.msg = publier ? 'Compte rendu publié.' : 'Brouillon enregistré.'; this.charger(); }, error: this.fail });
  }
}
