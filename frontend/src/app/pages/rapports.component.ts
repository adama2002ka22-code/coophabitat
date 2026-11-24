import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe, KeyValuePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

@Component({
  selector: 'app-rapports',
  standalone: true,
  imports: [FormsModule, DecimalPipe, DatePipe, KeyValuePipe],
  template: `
  <div class="dash-hero anim-in">
    <span class="role-chip" style="margin-bottom:.4rem">EXPORTS JASPERREPORTS PDF &amp; EXCEL</span>
    <h1>Rapports financiers semestriels</h1>
    <span class="muted">Bilan comptable semestriel, suivi budgétaire, journal d'audit et exports officiels PDF / XLSX</span>
  </div>
  @if (err) { <div class="msg err">{{ err }}</div> }
  <div class="card"><div class="form-grid">
    <div><label>Année</label><input type="number" [(ngModel)]="annee"></div>
    <div><label>Semestre</label><select [(ngModel)]="semestre"><option [ngValue]="1">Semestre 1 (janv.-juin)</option><option [ngValue]="2">Semestre 2 (juil.-déc.)</option></select></div></div>
    <p class="btns"><button class="btn primary" (click)="charger()">Afficher</button>
      <button class="btn" (click)="export('pdf')">Exporter PDF</button>
      <button class="btn" (click)="export('excel')">Exporter Excel</button></p></div>

  @if (r) {
    <div class="grid">
      <div class="kpi"><span>Budget prévisionnel {{ r.annee }}</span><b>{{ r.budgetPrevisionnel | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi ok"><span>Encaissements du semestre</span><b>{{ r.encaissements | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi err"><span>Dépenses réelles</span><b>{{ r.depenses | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi warn"><span>Trésorerie disponible</span><b>{{ r.tresorerieDisponible | number:'1.0-0' }} FCFA</b></div>
    </div>
    <div class="card"><h3>Dépenses par catégorie</h3>
      @for (e of r.depensesParCategorie | keyvalue; track e.key) { <p style="margin:.2rem 0">{{ e.key }} : <b>{{ $any(e.value) | number:'1.0-0' }} FCFA</b></p> }
      <p class="muted">Période : {{ r.debut | date:'dd/MM/yyyy' }} au {{ r.fin | date:'dd/MM/yyyy' }} · Membres à jour : {{ r.membresAJour }} · en retard : {{ r.membresEnRetard }}</p></div>
  }

  @if (auth.isBureau) {
    <div class="card"><div class="row"><h3 style="margin:0">Journal d'audit</h3><button class="btn" (click)="audit()">Charger</button></div>
      @if (logs.length) { <div class="tw"><table><thead><tr><th>Date</th><th>Acteur</th><th>Action</th><th>Détails</th></tr></thead><tbody>
        @for (l of logs; track l.id) { <tr><td>{{ l.dateAction | date:'dd/MM/yy HH:mm' }}</td><td>{{ l.acteur }}</td><td>{{ l.action }}</td><td>{{ l.details }}</td></tr> }
      </tbody></table></div> }
    </div>
  }`
})
export class RapportsComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);
  annee = new Date().getFullYear(); semestre = new Date().getMonth() < 6 ? 1 : 2;
  r: any = null; logs: any[] = []; err = '';
  private fail = (e: any) => this.err = errMsg(e);
  ngOnInit() { this.charger(); }
  charger() { this.err = ''; this.api.get('/rapports/financier', { annee: this.annee, semestre: this.semestre }).subscribe({ next: r => this.r = r, error: this.fail }); }
  export(f: 'pdf' | 'excel') {
    this.err = '';
    this.api.download(`/rapports/financier/${f}?annee=${this.annee}&semestre=${this.semestre}`,
      `rapport-${this.annee}-S${this.semestre}.${f === 'pdf' ? 'pdf' : 'xlsx'}`, this.fail);
  }
  audit() { this.api.get<any[]>('/audit').subscribe({ next: l => this.logs = l, error: this.fail }); }
}
