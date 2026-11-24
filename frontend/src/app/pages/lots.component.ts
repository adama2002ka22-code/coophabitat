import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { errMsg } from '../core/util';

interface LotPresentation {
  image: string;
  accroche: string;
  atouts: string[];
}

@Component({
  selector: 'app-lots',
  standalone: true,
  imports: [FormsModule, DatePipe],
  template: `
  <div class="dash-hero estate anim-in">
    <div class="row" style="margin-bottom:0">
      <div>
        <span class="role-chip" style="margin-bottom:.4rem">FONCIER &amp; ATTRIBUTIONS PKCS#12</span>
        <h1>Lots &amp; patrimoine immobilier</h1>
        <span class="muted">Terrains viabilisés et logements géolocalisés · Tirage au sort auditable &amp; PV signés</span>
      </div>
      <div class="btns">
        <button class="l-btn l-btn-ghost" (click)="modeAffichage = modeAffichage === 'CARTES' ? 'TABLEAU' : 'CARTES'">
          {{ modeAffichage === 'CARTES' ? 'Vue tableau' : 'Vue fiches photos' }}
        </button>
        @if (canManage) { <button class="l-btn l-btn-primary" (click)="formLot = !formLot">{{ formLot ? 'Fermer' : '+ Ajouter un lot' }}</button> }
      </div>
    </div>
  </div>
  @if (msg) { <div class="msg ok">{{ msg }}</div> }
  @if (err) { <div class="msg err">{{ err }}</div> }

  @if (formLot && canManage) {
    <div class="card anim-in"><h3>Nouveau lot foncier</h3><div class="form-grid">
      <div><label>Référence</label><input [(ngModel)]="nl.reference" placeholder="Lot C-05"></div>
      <div><label>Type</label><select [(ngModel)]="nl.typeLot"><option value="TERRAIN">Terrain</option><option value="LOGEMENT">Logement</option></select></div>
      <div><label>Superficie (m²)</label><input type="number" [(ngModel)]="nl.superficie"></div>
      <div><label>Localisation</label><input [(ngModel)]="nl.localisation" placeholder="Diamniadio, Dakar"></div>
      <div><label>Latitude GPS</label><input type="number" step="0.0001" [(ngModel)]="nl.latitude" placeholder="14.6937"></div>
      <div><label>Longitude GPS</label><input type="number" step="0.0001" [(ngModel)]="nl.longitude" placeholder="-17.4441"></div>
    </div><p style="margin-bottom:0"><button class="btn primary" (click)="creerLot()">Enregistrer le lot</button></p></div>
  }

  <div class="filter-bar">
    <input [(ngModel)]="search" placeholder="Rechercher référence, quartier (Almadies, Diamniadio...)...">
    <div class="tabs" style="margin:0">
      <button [class.on]="filtre === 'TOUS'" (click)="filtre = 'TOUS'">Tous ({{ lots.length }})</button>
      <button [class.on]="filtre === 'DISPONIBLE'" (click)="filtre = 'DISPONIBLE'">Disponibles</button>
      <button [class.on]="filtre === 'ATTRIBUE'" (click)="filtre = 'ATTRIBUE'">Attribués</button>
      <button [class.on]="filtre === 'LITIGIEUX'" (click)="filtre = 'LITIGIEUX'">Litigieux</button>
    </div>
  </div>

  <!-- VUE FICHES IMMOBILIERES AVEC PHOTOS & ELOGE DU LOT -->
  @if (modeAffichage === 'CARTES') {
    <div class="lot-grid anim-in">
      @for (l of filtres; track l.id) {
        @let p = presentation(l);
        <article class="lot-card">
          <div class="lot-media">
            <img [src]="p.image" [alt]="l.reference + ' - ' + l.localisation" loading="lazy">
            <div class="lot-media-top">
              <span class="lot-type-pill">{{ l.typeLot === 'LOGEMENT' ? 'LOGEMENT / VILLA' : 'TERRAIN VIABILISÉ' }}</span>
              <span class="badge" [class.ok]="l.statut === 'ATTRIBUE'" [class.warn]="l.statut === 'DISPONIBLE'" [class.err]="l.statut === 'LITIGIEUX'">{{ l.statut }}</span>
            </div>
            <div class="lot-media-bottom">
              <div>
                <b>{{ l.reference }}</b>
                <span>📍 {{ l.localisation || 'Dakar, Sénégal' }}</span>
              </div>
              <span class="lot-area">{{ l.superficie }} m²</span>
            </div>
          </div>

          <div class="lot-body">
            <p class="lot-desc">{{ p.accroche }}</p>

            <div class="lot-atouts">
              @for (a of p.atouts; track a) { <span class="lot-atout">✓ {{ a }}</span> }
            </div>

            <div class="lot-footer">
              <div class="row" style="margin:0;font-size:.82rem">
                <span class="muted">
                  @if (l.beneficiaire) { Bénéficiaire : <b style="color:var(--text)">{{ l.beneficiaire }}</b> }
                  @else { <b style="color:var(--ok)">Ouvert à l'attribution</b> }
                </span>
                @if (l.latitude != null) {
                  <button type="button" class="btn" style="padding:.22rem .6rem;font-size:.76rem" (click)="ouvrirCarte(l)">Carte GPS ↗</button>
                }
              </div>

              <div class="btns">
                @if (l.statut === 'DISPONIBLE' && canManage) { <button class="btn accent" (click)="tirage(l)">Tirage au sort</button> }
                @if (l.statut === 'DISPONIBLE' && auth.hasRole('PRESIDENT')) { <button class="btn" (click)="ouvrirCommission(l)">Commission</button> }
                @if ((l.statut === 'DISPONIBLE' || l.statut === 'LITIGIEUX') && canManage) {
                  <button class="btn" (click)="litige(l)">{{ l.statut === 'LITIGIEUX' ? 'Lever litige' : 'Litigieux' }}</button>
                }
                @if (l.statut === 'ATTRIBUE') {
                  @if (auth.isBureau || l.beneficiaire === auth.user?.nom) { <button class="btn primary" (click)="pv(l)">PV signé PDF</button> }
                  <button class="btn" (click)="verifier(l)">Vérifier signature</button>
                }
              </div>
            </div>
          </div>
        </article>
      } @empty {
        <div class="card"><p class="muted" style="margin:0">Aucun lot ne correspond à ce filtre.</p></div>
      }
    </div>
  } @else {
    <div class="card anim-in"><div class="tw"><table>
      <thead><tr><th>Référence</th><th>Type</th><th>Superficie</th><th>Localisation / GPS</th><th>Statut</th><th>Bénéficiaire</th><th>Actions</th></tr></thead><tbody>
      @for (l of filtres; track l.id) {
        <tr>
          <td><b>{{ l.reference }}</b></td>
          <td>{{ l.typeLot }}</td>
          <td>{{ l.superficie }} m²</td>
          <td>
            {{ l.localisation }}
            @if (l.latitude != null) {
              <br><span class="muted">{{ l.latitude }}, {{ l.longitude }}</span>
              · <button type="button" class="btn" style="padding:.15rem .5rem;font-size:.75rem" (click)="ouvrirCarte(l)">Voir carte</button>
            }
          </td>
          <td><span class="badge" [class.ok]="l.statut === 'ATTRIBUE'" [class.warn]="l.statut === 'DISPONIBLE'" [class.err]="l.statut === 'LITIGIEUX'">{{ l.statut }}</span></td>
          <td><b>{{ l.beneficiaire ?? '—' }}</b></td>
          <td class="btns">
            @if (l.statut === 'DISPONIBLE' && canManage) { <button class="btn accent" (click)="tirage(l)">Tirage au sort</button> }
            @if (l.statut === 'DISPONIBLE' && auth.hasRole('PRESIDENT')) { <button class="btn" (click)="ouvrirCommission(l)">Commission</button> }
            @if ((l.statut === 'DISPONIBLE' || l.statut === 'LITIGIEUX') && canManage) {
              <button class="btn" (click)="litige(l)">{{ l.statut === 'LITIGIEUX' ? 'Lever litige' : 'Litigieux' }}</button>
            }
            @if (l.statut === 'ATTRIBUE') {
              @if (auth.isBureau || l.beneficiaire === auth.user?.nom) { <button class="btn primary" (click)="pv(l)">PV signé PDF</button> }
              <button class="btn" (click)="verifier(l)">Vérifier signature</button>
            }
          </td>
        </tr>
      }
    </tbody></table></div></div>
  }

  @if (mapLot && mapUrl) {
    <div class="card anim-in">
      <div class="row">
        <div>
          <h3 style="margin:0">Localisation GPS : {{ mapLot.reference }} — {{ mapLot.localisation }}</h3>
          <span class="muted">Coordonnées exactes : {{ mapLot.latitude }}, {{ mapLot.longitude }}</span>
        </div>
        <div class="btns">
          <a target="_blank" rel="noopener" [href]="carteLien(mapLot)" class="btn">Ouvrir OpenStreetMap ↗</a>
          <button class="btn" (click)="mapLot = null">Fermer la carte</button>
        </div>
      </div>
      <iframe class="map-box" [src]="mapUrl" loading="lazy" title="Carte GPS du lot"></iframe>
    </div>
  }

  @if (resultat) {
    <div class="card anim-in" style="border-left:4px solid var(--ok)">
      <div class="row">
        <h3 style="margin:0">Résultat officiel · {{ resultat.pvNumero }}</h3>
        <button class="btn primary" (click)="telechargerResultatPv()">Télécharger le PV signé (PKCS#12)</button>
      </div>
      <p><b>{{ resultat.lotReference }}</b> attribué à <b>{{ resultat.beneficiaireNom }}</b> ({{ resultat.beneficiaireNumero }}) le {{ resultat.dateAttribution | date:'dd/MM/yyyy HH:mm' }}</p>
      <p class="muted">{{ resultat.details }}</p>
      <p class="muted" style="font-size:.78rem;word-break:break-all">Empreinte SHA-256 : {{ resultat.pvHash }}</p>
    </div>
  }

  @if (commission) {
    <div class="card anim-in"><h3>Attribution par commission : {{ commission.lot.reference }}</h3>
      <div class="form-grid">
        <div><label>Bénéficiaire</label><select [(ngModel)]="commission.membreId">
          <option [ngValue]="null">Choisir un membre actif...</option>
          @for (m of membres; track m.id) { <option [ngValue]="m.id">{{ m.nom }} ({{ m.numeroAdherent }})</option> }</select></div>
        <div><label>Membres de la commission</label><input [(ngModel)]="commission.membresCommission" placeholder="Président, Trésorier, Secrétaire"></div>
      </div>
      <label>Justification officielle</label><textarea rows="3" [(ngModel)]="commission.justification"></textarea>
      <p class="btns" style="margin-bottom:0"><button class="btn primary" (click)="validerCommission()">Valider et signer le PV</button><button class="btn" (click)="commission = null">Annuler</button></p>
    </div>
  }`
})
export class LotsComponent implements OnInit {
  private api = inject(ApiService);
  private sanitizer = inject(DomSanitizer);
  auth = inject(AuthService);
  lots: any[] = []; membres: any[] = []; formLot = false; msg = ''; err = '';
  resultat: any = null; commission: any = null;
  modeAffichage: 'CARTES' | 'TABLEAU' = 'CARTES';
  filtre: 'TOUS' | 'DISPONIBLE' | 'ATTRIBUE' | 'LITIGIEUX' = 'TOUS';
  search = '';
  mapLot: any = null; mapUrl: SafeResourceUrl | null = null;
  nl: any = { reference: '', typeLot: 'TERRAIN', superficie: 300, localisation: '', latitude: null, longitude: null };

  get canManage() { return this.auth.hasRole('PRESIDENT', 'SECRETAIRE'); }
  get filtres() {
    const q = this.search.trim().toLowerCase();
    return this.lots.filter(l => {
      if (this.filtre !== 'TOUS' && l.statut !== this.filtre) return false;
      if (!q) return true;
      return `${l.reference} ${l.localisation ?? ''} ${l.typeLot} ${l.beneficiaire ?? ''}`.toLowerCase().includes(q);
    });
  }

  presentation(l: any): LotPresentation {
    const loc = String(l.localisation ?? '').toLowerCase();
    const ref = String(l.reference ?? '').toLowerCase();
    if (loc.includes('almadies') || ref.includes('v-01')) {
      return {
        image: '/images/hero-residence.jpg',
        accroche: 'Résidence contemporaine haut standing à quelques minutes de l’océan Atlantique et de la Corniche Ouest. Cadre lumineux, brise marine, sécurité 24h/24 et proximité immédiate des commerces et écoles internationales.',
        atouts: ['Proche mer (5 min)', 'Corniche Ouest', 'Standing R+1', 'Accès transport rapide']
      };
    }
    if (loc.includes('diamniadio')) {
      return {
        image: '/images/lotissement-vue.jpg',
        accroche: 'Parcelle spacieuse au cœur du pôle urbain d’avenir de Diamniadio. Accès direct au TER (8 min) et à l’autoroute à péage, voirie aménagée, zone résidentielle en plein essor avec fort potentiel de valorisation.',
        atouts: ['TER Diamniadio (8 min)', 'Autoroute à péage', 'Terrain borné VRD', 'Fort potentiel']
      };
    }
    if (ref.includes('a-02')) {
      return {
        image: '/images/villa-projet.jpg',
        accroche: 'Superbe emplacement résidentiel dans un secteur calme et arboré de Cité Keur Gorgui. Voirie pavée, réseau eau et électricité prêt, idéal pour construire une villa familiale à 12 min du Plateau et de la mer.',
        atouts: ['Quartier calme & sécurisé', 'VDN & BRT à 5 min', 'Proche écoles & cliniques', 'Eau & Électricité']
      };
    }
    if (loc.includes('keur gorgui') || ref.includes('a-01')) {
      return {
        image: '/images/lotissement-vue.jpg',
        accroche: 'Terrain privilégié au cœur de Cité Keur Gorgui à Dakar. Connexion immédiate à la VDN, aux transports urbains (BRT) et aux centres d’affaires. Parcelle parfaitement plane, bornée et prête à bâtir.',
        atouts: ['Cœur de Dakar', 'Accès VDN & BRT 5 min', 'Titre foncier sécurisé', 'Zone viabilisée']
      };
    }
    return l.typeLot === 'LOGEMENT'
      ? {
          image: '/images/villa-projet.jpg',
          accroche: 'Logement moderne conçu avec des matériaux durables, espaces lumineux, accès facile aux axes routiers principaux et aux commodités de proximité.',
          atouts: ['Architecture moderne', 'Accès transports', 'Cadre sécurisé', 'Foncier audité']
        }
      : {
          image: '/images/lotissement-vue.jpg',
          accroche: 'Parcelle viabilisée et géolocalisée au sein du programme CoopHabitat, avec voirie accessible, bornage officiel et titre foncier garanti par la coopérative.',
          atouts: ['Parcelle bornée', 'Accès voirie & VRD', 'Coordonnées GPS', 'Attribution transparente']
        };
  }

  ngOnInit() { this.charger(); }
  carteLien(l: any) { return `https://www.openstreetmap.org/?mlat=${l.latitude}&mlon=${l.longitude}#map=16/${l.latitude}/${l.longitude}`; }
  ouvrirCarte(l: any) {
    this.mapLot = l;
    const d = 0.01;
    const url = `https://www.openstreetmap.org/export/embed.html?bbox=${l.longitude - d},${l.latitude - d},${l.longitude + d},${l.latitude + d}&layer=mapnik&marker=${l.latitude},${l.longitude}`;
    this.mapUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
  private reset() { this.msg = ''; this.err = ''; }
  charger() { this.api.get<any[]>('/lots').subscribe({ next: l => this.lots = l, error: e => this.err = errMsg(e) }); }
  creerLot() {
    this.reset();
    this.api.post('/lots', this.nl).subscribe({ next: () => { this.msg = 'Lot enregistré avec succès.'; this.formLot = false; this.charger(); }, error: e => this.err = errMsg(e) });
  }
  tirage(l: any) {
    if (!confirm(`Lancer le tirage au sort officiel pour le lot ${l.reference} parmi les membres actifs à jour sans lot ?`)) return;
    this.reset();
    this.api.post(`/lots/${l.id}/tirage`).subscribe({
      next: a => { this.resultat = a; this.msg = `Tirage au sort effectué ! PV ${a.pvNumero} signé (PKCS#12) et bénéficiaire notifié.`; this.charger(); },
      error: e => this.err = errMsg(e)
    });
  }
  ouvrirCommission(l: any) {
    this.commission = { lot: l, membreId: null, membresCommission: '', justification: '' };
    if (!this.membres.length) this.api.get<any[]>('/membres').subscribe(m => this.membres = m.filter(x => x.statut === 'ACTIF'));
  }
  validerCommission() {
    this.reset();
    const c = this.commission;
    this.api.post(`/lots/${c.lot.id}/commission`, { membreId: c.membreId, membresCommission: c.membresCommission, justification: c.justification })
      .subscribe({ next: a => { this.resultat = a; this.commission = null; this.msg = `Attribution validée ! PV ${a.pvNumero} signé (PKCS#12) et bénéficiaire notifié.`; this.charger(); }, error: e => this.err = errMsg(e) });
  }
  litige(l: any) {
    this.reset();
    this.api.patch(`/lots/${l.id}/statut`, { statut: l.statut === 'LITIGIEUX' ? 'DISPONIBLE' : 'LITIGIEUX' }).subscribe({ next: () => this.charger(), error: e => this.err = errMsg(e) });
  }
  pv(l: any) { this.api.download(`/attributions/${l.attributionId}/pv`, `PV-${l.reference}.pdf`, e => this.err = errMsg(e)); }
  telechargerResultatPv() {
    if (this.resultat) this.api.download(`/attributions/${this.resultat.id}/pv`, `${this.resultat.pvNumero}.pdf`, e => this.err = errMsg(e));
  }
  verifier(l: any) {
    this.reset();
    this.api.get(`/attributions/${l.attributionId}/verification`).subscribe({
      next: v => { if (v.valide) this.msg = `PV ${v.pvNumero} : signature valide, intégrité cryptographique vérifiée (SHA-256).`; else this.err = `PV ${v.pvNumero} : SIGNATURE INVALIDE.`; },
      error: e => this.err = errMsg(e)
    });
  }
}
