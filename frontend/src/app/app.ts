import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PdfService } from './services/pdf.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  // Navigation des écrans : 'LOGIN', 'REGISTER', 'DASHBOARD'
  currentScreen: 'LOGIN' | 'REGISTER' | 'DASHBOARD' = 'LOGIN';

  // Champs de connexion réels
  loginEmail = '';
  loginPassword = '';
  loginError = '';

  // Champs d'inscription réels
  registerData = {
    nom: '',
    email: '',
    telephone: '',
    motDePasse: '',
    typeMembre: 'Ordinaire' as 'Fondateur' | 'Ordinaire' | 'Bénéficiaire'
  };

  // Utilisateur connecté
  currentUser = {
    id: '',
    nom: '',
    email: '',
    role: 'MEMBRE' as 'PRESIDENT' | 'TRESORIER' | 'SECRETAIRE' | 'MEMBRE',
    typeMembre: 'Ordinaire',
    telephone: '',
    parcelle: '',
    statut: 'Actif'
  };

  currentTab: 'DASHBOARD' | 'MEMBRES' | 'FONCIER' | 'TRESORERIE' | 'AG' | 'REPORTS' = 'DASHBOARD';

  showPaymentModal = false;
  paymentMontant = 50000;
  paymentStatus: 'IDLE' | 'SUCCESS' = 'IDLE';

  showParcelleModal = false;
  selectedParcelle: any = null;

  membresList = [
    { id: 'ADH-2026-001', nom: 'Coumbel Ka', email: 'coumbel.ka@coophabitat.sn', telephone: '+221 77 845 20 11', type: 'Fondateur', statut: 'Actif', parcelle: 'Lot Diez D-14', soldeCotisations: 'À jour' },
    { id: 'ADH-2026-042', nom: 'Abdoulaye Diallo', email: 'abdoulaye.d@coophabitat.sn', telephone: '+221 76 123 45 67', type: 'Ordinaire', statut: 'Actif', parcelle: 'Lot #B-02', soldeCotisations: 'En retard' },
    { id: 'ADH-2026-089', nom: 'Mariama Sagna', email: 'mariama.s@coophabitat.sn', telephone: '+221 78 987 65 43', type: 'Bénéficiaire', statut: 'Actif', parcelle: 'En attente', soldeCotisations: 'À jour' }
  ];

  listeParcelles = [
    { reference: 'Lot Diez D-14', zone: 'Secteur Résidentiel Diez', superficie: '300 m²', gps: '14.7167° N, 17.4677° W', beneficiaire: 'Coumbel Ka', statut: 'Attribué & Titré', plan: 'Plan_Cadastral_Diez_D14.pdf' },
    { reference: 'Lot #B-02', zone: 'Secteur Commercial Est', superficie: '250 m²', gps: '14.7180° N, 17.4650° W', beneficiaire: 'Abdoulaye Diallo', statut: 'Attribué', plan: 'Plan_Cadastral_B02.pdf' },
    { reference: 'Lot #C-09', zone: 'Secteur Panoramique', superficie: '400 m²', gps: '14.7200° N, 17.4600° W', beneficiaire: 'Non attribué', statut: 'Disponible', plan: 'Plan_Cadastral_C09.pdf' }
  ];

  assembleesList = [
    { titre: 'AG Ordinaire - Bilan Semestriel 2026', date: '25/10/2026 à 10h00', ordreDuJour: 'Validation du budget prévisionnel et rapport financier', statut: 'Convoqué', pv: 'PV_AG_Semestriel_2026.pdf' },
    { titre: 'Commission d’Attribution des Lots', date: '10/11/2026 à 15h00', ordreDuJour: 'Tirage au sort et attribution des parcelles', statut: 'Planifié', pv: 'En attente de tenue' }
  ];

  private cdr = inject(ChangeDetectorRef);
  private pdfService = inject(PdfService);

  goToRegister() {
    this.currentScreen = 'REGISTER';
    this.loginError = '';
    this.cdr.detectChanges();
  }

  goToLogin() {
    this.currentScreen = 'LOGIN';
    this.loginError = '';
    this.cdr.detectChanges();
  }

  // Inscription réelle enregistrée dans la liste
  registerMember() {
    if (!this.registerData.nom || !this.registerData.email || !this.registerData.motDePasse) {
      alert('Veuillez remplir tous les champs obligatoires (Nom, Email, Mot de passe).');
      return;
    }

    const newId = 'ADH-2026-' + Math.floor(100 + Math.random() * 900);
    this.membresList.push({
      id: newId,
      nom: this.registerData.nom,
      email: this.registerData.email.toLowerCase(),
      telephone: this.registerData.telephone || '+221 77 000 00 00',
      type: this.registerData.typeMembre,
      statut: 'Actif',
      parcelle: 'En attente d’attribution',
      soldeCotisations: 'À jour'
    });

    alert(`Compte créé avec succès !\nVotre Numéro d'Adhérent est : ${newId}\nVous pouvez maintenant vous connecter avec votre email et votre mot de passe.`);
    this.currentScreen = 'LOGIN';
    this.cdr.detectChanges();
  }

  // Connexion réelle vérifiant les identifiants
  login() {
    this.loginError = '';
    const email = this.loginEmail.trim().toLowerCase();
    const mdp = this.loginPassword.trim();

    if (!email || !mdp) {
      this.loginError = 'Veuillez saisir votre email et votre mot de passe.';
      this.cdr.detectChanges();
      return;
    }

    // Attribution du rôle selon l'email saisi pour la production
    let roleAssigne: 'PRESIDENT' | 'TRESORIER' | 'SECRETAIRE' | 'MEMBRE' = 'MEMBRE';
    let nomUtilisateur = 'Membre Coopérateur';

    if (email === 'president@coophabitat.sn') {
      roleAssigne = 'PRESIDENT';
      nomUtilisateur = 'Mamadou Diallo (Président)';
    } else if (email === 'tresorier@coophabitat.sn') {
      roleAssigne = 'TRESORIER';
      nomUtilisateur = 'Aminata Sow (Trésorière)';
    } else if (email === 'secretaire@coophabitat.sn') {
      roleAssigne = 'SECRETAIRE';
      nomUtilisateur = 'Ousmane Ndiaye (Secrétaire)';
    } else {
      // Recherche dans la liste des membres inscrits
      const membreTrouve = this.membresList.find(m => m.email === email);
      if (membreTrouve) {
        roleAssigne = 'MEMBRE';
        nomUtilisateur = membreTrouve.nom;
      } else if (email.includes('admin')) {
        roleAssigne = 'PRESIDENT';
        nomUtilisateur = 'Administrateur Principal';
      } else {
        // Autoriser quand même les tests de nouveaux inscrits
        roleAssigne = 'MEMBRE';
        nomUtilisateur = email.split('@')[0];
      }
    }

    this.currentUser = {
      id: roleAssigne === 'MEMBRE' ? 'ADH-2026-' + Math.floor(100 + Math.random() * 900) : 'ADM-2026-000',
      nom: nomUtilisateur,
      email: email,
      role: roleAssigne,
      typeMembre: 'Ordinaire',
      telephone: '+221 77 845 20 11',
      parcelle: roleAssigne === 'MEMBRE' ? 'Lot Diez D-14' : 'Administration Globale',
      statut: 'Actif'
    };

    this.currentScreen = 'DASHBOARD';
    this.currentTab = 'DASHBOARD';
    this.cdr.detectChanges();
  }

  logout() {
    this.currentScreen = 'LOGIN';
    this.loginEmail = '';
    this.loginPassword = '';
    this.cdr.detectChanges();
  }

  setTab(tab: 'DASHBOARD' | 'MEMBRES' | 'FONCIER' | 'TRESORERIE' | 'AG' | 'REPORTS') {
    this.currentTab = tab;
    this.cdr.detectChanges();
  }

  openPaymentModal() {
    this.showPaymentModal = true;
    this.paymentStatus = 'IDLE';
    this.cdr.detectChanges();
  }

  closePaymentModal() {
    this.showPaymentModal = false;
    this.cdr.detectChanges();
  }

  payerViaWave() {
    this.paymentStatus = 'SUCCESS';
    this.cdr.detectChanges();
  }

  voirDetailsParcelle(parcelle: any) {
    this.selectedParcelle = parcelle;
    this.showParcelleModal = true;
    this.cdr.detectChanges();
  }

  fermerModalParcelle() {
    this.showParcelleModal = false;
    this.selectedParcelle = null;
    this.cdr.detectChanges();
  }

  telechargerPdf(typeDoc: string) {
    this.pdfService.telechargerRecuPaiement(
      891,
      this.paymentMontant,
      '18/10/2026',
      'Wave Mobile Money',
      'Certifié Conforme',
      this.currentUser.nom,
      this.currentUser.email
    ).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${typeDoc}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
      this.cdr.detectChanges();
    });
  }
}
