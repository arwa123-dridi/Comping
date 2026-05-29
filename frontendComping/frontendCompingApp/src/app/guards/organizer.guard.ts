// src/app/guards/organizer.guard.ts
import { Injectable } from '@angular/core';
import {
  CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, Router, UrlTree
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class OrganizerGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    const token    = localStorage.getItem('authToken');
    const userId   = localStorage.getItem('userId');
    const userRole = localStorage.getItem('userRole');

    const isOrg   = userRole === 'ORGANISATEUR' || userRole === 'ROLE_ORGANISATEUR';
    const isAdmin  = userRole === 'ADMIN'        || userRole === 'ROLE_ADMIN';

    // Autorise ADMIN + ORGANISATEUR
    if (token && userId && (isOrg || isAdmin)) {
      return true;
    }

    if (token && userId) {
      // Connecté mais pas autorisé → dashboard user
      return this.router.createUrlTree(['/dashboard'], {
        queryParams: { error: 'organizer-required' }
      });
    }

    // Non connecté → login
    localStorage.setItem('redirect_after_login', state.url);
    return this.router.createUrlTree(['/login']);
  }
}
