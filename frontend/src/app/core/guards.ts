import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLogged ? true : inject(Router).createUrlTree(['/login']);
};

export const roleGuard: CanActivateFn = route => {
  const auth = inject(AuthService);
  const roles: string[] = route.data['roles'] ?? [];
  return auth.hasRole(...roles) ? true : inject(Router).createUrlTree(['/app/accueil']);
};
