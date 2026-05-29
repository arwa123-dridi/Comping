import { Injectable } from '@angular/core';
import {
  CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, Router, UrlTree
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    const token       = localStorage.getItem('authToken');
    const userId      = localStorage.getItem('userId');
    const tokenExpiry = localStorage.getItem('tokenExpiry');

    // Vérifier si le token est expiré
    if (token && tokenExpiry && new Date(tokenExpiry) < new Date()) {
      this.clearSession();
      // Stocke l'URL pour rediriger après login
      localStorage.setItem('redirect_after_login', state.url);
      return this.router.createUrlTree(['/login'], {
        queryParams: { expired: 'true' }
      });
    }

    // Vérifier si l'utilisateur est connecté (token présent)
    if (token && userId && userId !== '' && userId !== 'undefined') {
      return true;
    }

    // Non connecté : Stocke l'URL cible et redirige vers /login
    localStorage.setItem('redirect_after_login', state.url);
    return this.router.createUrlTree(['/login']);
  }

  private clearSession(): void {
    ['authToken','userId','userEmail','userNom','userRole','tokenExpiry']
      .forEach(k => localStorage.removeItem(k));
  }
}
