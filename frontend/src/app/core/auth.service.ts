import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ROLES_BUREAU } from './util';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private _user: any = JSON.parse(localStorage.getItem('coop_user') || 'null');

  get token(): string | null { return localStorage.getItem('coop_token'); }
  get user(): any { return this._user; }
  get isLogged(): boolean { return !!this.token && !!this._user; }
  get isBureau(): boolean { return this.hasRole(...ROLES_BUREAU); }
  hasRole(...roles: string[]): boolean { return !!this._user && roles.includes(this._user.role); }

  login(email: string, motDePasse: string) {
    return this.http.post<any>(`${environment.apiUrl}/auth/login`, { email, motDePasse }).pipe(
      tap(r => {
        localStorage.setItem('coop_token', r.token);
        localStorage.setItem('coop_user', JSON.stringify(r.membre));
        this._user = r.membre;
      }));
  }

  register(data: any) { return this.http.post<any>(`${environment.apiUrl}/auth/register`, data); }

  logout() {
    localStorage.removeItem('coop_token');
    localStorage.removeItem('coop_user');
    this._user = null;
    // Le cache hors-ligne des donnees d'API est purge : jamais de donnees d'un autre utilisateur sur l'appareil
    if ('caches' in window) {
      caches.keys().then(keys => keys.filter(k => k.includes('ngsw') && k.includes('data')).forEach(k => caches.delete(k)));
    }
    this.router.navigate(['/login']);
  }
}
