import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

@Component({
  selector: 'app-comptabilite',
  standalone: true,
  imports: [FormsModule, DatePipe, DecimalPipe],
  template: `
  <div class="dash-hero anim-in">
    <span class="role-chip" style="margin-bottom:.4rem">TRÉSORERIE &amp; SÉQUESTRE COMMUN</span>
    <h1>Espace comptable</h1>
    <span class="muted">Transparence totale sur le séquestre, le budget prévisionnel, les dépenses et le plan de cotisation</span>
  </div>
  <div class="tabs">
    @for (t of tabs; track t.id) { <button [class.on]="tab === t.id" (click)="aller(t.id)">{{ t.label }}</button> }
  </div>
  @if (msg) { <div class="msg ok">{{ msg }}</div> }
  @if (err) { <div class="msg err">{{ err }}</div> }

  @if (tab === 'sequestre' && seq) {
    <div class="grid">
      <div class="kpi warn"><span>Trésorerie disponible (séquestre)</span><b>{{ seq.tresorerieDisponible | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi ok"><span>Total encaissé</span><b>{{ seq.totalEncaisse | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi err"><span>Dépenses réelles</span><b>{{ seq.totalDepenses | number:'1.0-0' }} FCFA</b></div>
      <div class="kpi"><span>Budget prévisionnel {{ annee }}</span><b>{{ totalBudget | number:'1.0-0' }} FCFA</b></div>
    </div>
    <p class="muted">Le fonds commun est visible par tous les membres.</p>
    @if (gestion) {
      <div class="card"><h3>Actions du trésorier</h3><p class="btns">
        <button class="btn primary" [disabled]="!online" (click)="generer()">Générer les échéances</button>
        <button class="btn accent" [disabled]="!online" (click)="rappels()">Envoyer les rappels J-3 maintenant</button></p>
        <p class="muted">Ces actions s'exécutent aussi automatiquement chaque jour à 7 h (heure de Dakar).</p></div>
    }
  }

  @if (tab === 'budget') {
    <div class="card"><h3>Budget prévisionnel {{ annee }}</h3><div class="tw"><table><thead><tr><th>Catégorie</th><th>Libellé</th><th>Prévu</th></tr></thead><tbody>
      @for (b of budget; track b.id) { <tr><td>{{ b.categorie }}</td><td>{{ b.libelle }}</td><td>{{ b.montantPrevu | number:'1.0-0' }} FCFA</td></tr> }
      <tr><td colspan="2"><b>Total</b></td><td><b>{{ totalBudget | number:'1.0-0' }} FCFA</b></td></tr></tbody></table></div></div>
    @if (gestion) {
      <div class="card"><h3>Ajouter une ligne</h3><div class="form-grid">
        <div><label>Catégorie</label><input [(ngModel)]="nb.categorie"></div>
        <div><label>Libellé</label><input [(ngModel)]="nb.libelle"></div>
        <div><label>Montant prévu</label><input type="number" [(ngModel)]="nb.montantPrevu"></div></div>
        <p><button class="btn primary" (click)="ajouterBudget()">Ajouter</button></p></div>
    }
  }

  @if (tab === 'depenses') {
    <div class="card"><h3>Dépenses réelles</h3><div class="tw"><table><thead><tr><th>Date</th><th>Catégorie</th><th>Libellé</th><th>Montant</th></tr></thead><tbody>
      @for (d of depenses; track d.id) { <tr><td>{{ d.dateDepense | date:'dd/MM/yyyy' }}</td><td>{{ d.categorie }}</td><td>{{ d.libelle }}</td><td>{{ d.montant | number:'1.0-0' }} FCFA</td></tr> }
    </tbody></table></div></div>
    @if (gestion) {
      <div class="card"><h3>Enregistrer une dépense</h3><div class="form-grid">
        <div><label>Catégorie</label><input [(ngModel)]="nd.categorie"></div>
        <div><label>Libellé</label><input [(ngModel)]="nd.libelle"></div>
        <div><label>Montant</label><input type="number" [(ngModel)]="nd.montant"></div>
        <div><label>Date</label><input type="date" [(ngModel)]="nd.dateDepense"></div></div>
        <p><button class="btn primary" (click)="ajouterDepense()">Enregistrer</button></p></div>
    }
  }

  @if (tab === 'paiements' && auth.isBureau) {
    @if (gestion) {
      <div class="card"><h3>Saisie manuelle d'un paiement</h3><div class="form-grid">
        <div><label>Membre</label><select [(ngModel)]="np.membreId"><option [ngValue]="null">Choisir...</option>
          @for (m of membres; track m.id) { <option [ngValue]="m.id">{{ m.nom }} ({{ m.numeroAdherent }})</option> }</select></div>
        <div><label>Montant</label><input type="number" [(ngModel)]="np.montant"></div>
        <div><label>Canal</label><select [(ngModel)]="np.canal"><option value="ESPECES">Espèces</option><option value="VIREMENT">Virement</option><option value="WAVE">Wave</option><option value="SENDFLASH">SendFlash</option></select></div>
        <div><label>Référence (optionnel)</label><input [(ngModel)]="np.reference"></div></div>
        <p><button class="btn primary" (click)="saisirPaiement()">Enregistrer et générer le reçu</button></p></div>
    }
    <div class="card"><h3>Derniers paiements</h3><div class="tw"><table><thead><tr><th>Reçu</th><th>Date</th><th>Membre</th><th>Canal</th><th>Montant</th><th></th></tr></thead><tbody>
      @for (p of paiements; track p.id) {
        <tr><td>{{ p.recuNumero }}</td><td>{{ p.datePaiement | date:'dd/MM/yyyy HH:mm' }}</td><td>{{ p.membreNom }}</td><td>{{ p.canal }}</td>
          <td>{{ p.montant | number:'1.0-0' }} FCFA</td><td><button class="btn" (click)="recu(p)">Reçu PDF</button></td></tr> }
    </tbody></table></div></div>
  }

  @if (tab === 'plan' && plan) {
    <div class="card"><h3>Plan de cotisation actif</h3>
      <div class="form-grid">
        <div><label>Montant mensuel (FCFA)</label><input type="number" [(ngModel)]="plan.montantMensuel" [disabled]="!gestion"></div>
        <div><label>Jour d'échéance (1-28)</label><input type="number" [(ngModel)]="plan.jourEcheance" [disabled]="!gestion"></div>
        <div><label>Pénalité (% du dû par jour de retard)</label><input type="number" step="0.01" [(ngModel)]="plan.tauxPenaliteJournalier" [disabled]="!gestion"></div>
        <div><label>Pénalité fixe (FCFA)</label><input type="number" [(ngModel)]="plan.penaliteFixe" [disabled]="!gestion"></div>
        <div><label>Délai de grâce (jours)</label><input type="number" [(ngModel)]="plan.delaiGraceJours" [disabled]="!gestion"></div></div>
      @if (gestion) { <p><button class="btn primary" (click)="sauverPlan()">Enregistrer le plan</button></p> }
      <p class="muted">Les pénalités de retard sont recalculées automatiquement et figées une fois l'échéance soldée.</p></div>
  }`
})
export class ComptabiliteComponent implements OnInit {
  private api = inject(ApiService);
  auth = inject(AuthService);
  tab = 'sequestre'; msg = ''; err = '';
  seq: any = null; budget: any[] = []; depenses: any[] = []; paiements: any[] = []; membres: any[] = []; plan: any = null;
  annee = new Date().getFullYear();
  nb: any = { categorie: '', libelle: '', montantPrevu: null };
  nd: any = { categorie: '', libelle: '', montant: null, dateDepense: '' };
  np: any = { membreId: null, montant: null, canal: 'ESPECES', reference: '' };
  get gestion() { return this.auth.hasRole('PRESIDENT', 'TRESORIER'); }
  get online() { return navigator.onLine; }
  get totalBudget() { return this.budget.reduce((s, b) => s + b.montantPrevu, 0); }
  get tabs() {
    const t = [{ id: 'sequestre', label: 'Séquestre' }, { id: 'budget', label: 'Budget' }, { id: 'depenses', label: 'Dépenses' }];
    if (this.auth.isBureau) t.push({ id: 'paiements', label: 'Paiements' });
    t.push({ id: 'plan', label: 'Plan de cotisation' });
    return t;
  }
  ngOnInit() { this.aller('sequestre'); }
  private fail = (e: any) => this.err = errMsg(e);
  aller(t: string) {
    this.tab = t; this.msg = ''; this.err = '';
    if (t === 'sequestre') { this.api.get('/tresorerie/sequestre').subscribe({ next: s => this.seq = s, error: this.fail }); this.chargerBudget(); }
    if (t === 'budget') this.chargerBudget();
    if (t === 'depenses') this.chargerDepenses();
    if (t === 'paiements') { this.chargerPaiements(); if (this.gestion && !this.membres.length) this.api.get<any[]>('/membres').subscribe(m => this.membres = m); }
    if (t === 'plan') this.api.get('/plan-cotisation').subscribe({ next: p => this.plan = p, error: this.fail });
  }
  chargerBudget() { this.api.get<any[]>('/budget', { exercice: this.annee }).subscribe({ next: b => this.budget = b, error: this.fail }); }
  chargerDepenses() { this.api.get<any[]>('/depenses').subscribe({ next: d => this.depenses = d, error: this.fail }); }
  chargerPaiements() { this.api.get<any[]>('/paiements').subscribe({ next: p => this.paiements = p, error: this.fail }); }
  generer() { this.api.post('/cotisations/generer').subscribe({ next: r => this.msg = `${r.generees} échéance(s) générée(s).`, error: this.fail }); }
  rappels() { this.api.post('/cotisations/rappels').subscribe({ next: r => this.msg = `${r.rappelsEnvoyes} rappel(s) J-3 envoyé(s) (SMS / e-mail / notification).`, error: this.fail }); }
  ajouterBudget() {
    this.api.post('/budget', { ...this.nb, exercice: this.annee }).subscribe({ next: () => { this.msg = 'Ligne ajoutée.'; this.nb = { categorie: '', libelle: '', montantPrevu: null }; this.chargerBudget(); }, error: this.fail });
  }
  ajouterDepense() {
    const body = { ...this.nd, dateDepense: this.nd.dateDepense || null };
    this.api.post('/depenses', body).subscribe({ next: () => { this.msg = 'Dépense enregistrée.'; this.nd = { categorie: '', libelle: '', montant: null, dateDepense: '' }; this.chargerDepenses(); }, error: this.fail });
  }
  saisirPaiement() {
    this.api.post('/paiements', { ...this.np, reference: this.np.reference || null }).subscribe({
      next: p => { this.msg = `Paiement enregistré, reçu ${p.recuNumero}.`; this.np = { membreId: null, montant: null, canal: 'ESPECES', reference: '' }; this.chargerPaiements(); }, error: this.fail });
  }
  recu(p: any) { this.api.download(`/paiements/${p.id}/recu`, `${p.recuNumero}.pdf`, this.fail); }
  sauverPlan() {
    this.api.put('/plan-cotisation', this.plan).subscribe({ next: p => { this.plan = p; this.msg = 'Plan de cotisation mis à jour.'; }, error: this.fail });
  }
}
