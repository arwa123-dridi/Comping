// src/app/guards/auth.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, Router, UrlTree
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    const token      = localStorage.getItem('authToken');
    const userId     = localStorage.getItem('userId');
    const tokenExpiry = localStorage.getItem('tokenExpiry');

    // Token expiré
    if (token && tokenExpiry && new Date(tokenExpiry) < new Date()) {
      this.clearSession();
      return this.router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url, expired: 'true' }
      });
    }

    //  userId valide (pas vide, pas "undefined")
    if (token && userId && userId !== '' && userId !== 'undefined') {
      return true;
    }

    // Non connecté →  /login
    return this.router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url }
    });
  }

  private clearSession(): void {
    ['authToken','userId','userEmail','userNom','userRole','tokenExpiry']
      .forEach(k => localStorage.removeItem(k));
  }
}