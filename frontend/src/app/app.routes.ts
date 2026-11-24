import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards';
import { LandingComponent } from './pages/landing.component';
import { LoginComponent, RegisterComponent } from './pages/auth.pages';
import { LayoutComponent } from './pages/layout.component';
import { HomeComponent } from './pages/home.component';
import { MonEspaceComponent } from './pages/mon-espace.component';
import { MembresComponent } from './pages/membres.component';
import { LotsComponent } from './pages/lots.component';
import { ComptabiliteComponent } from './pages/comptabilite.component';
import { AssembleesComponent } from './pages/assemblees.component';
import { RapportsComponent } from './pages/rapports.component';
import { NotificationsComponent } from './pages/notifications.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'app', component: LayoutComponent, canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'accueil' },
      { path: 'accueil', component: HomeComponent },
      { path: 'mon-espace', component: MonEspaceComponent },
      { path: 'membres', component: MembresComponent, canActivate: [roleGuard], data: { roles: ['PRESIDENT', 'TRESORIER', 'SECRETAIRE'] } },
      { path: 'lots', component: LotsComponent },
      { path: 'comptabilite', component: ComptabiliteComponent },
      { path: 'assemblees', component: AssembleesComponent },
      { path: 'rapports', component: RapportsComponent },
      { path: 'notifications', component: NotificationsComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
