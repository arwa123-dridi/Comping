import { Injectable } from '@angular/core';
import {
  CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, Router, UrlTree
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class OrganizerOnlyGuard implements CanActivate {

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

    // Autorise seulement ORGANISATEUR (ADMIN non autorisé ici)
    if (token && userId && isOrg) {
      return true;
    }

    // Sinon → /dashboard ou /admin/dashboard selon rôle
    if (token && userId) {
      if (isAdmin) {
        return this.router.createUrlTree(['/admin/dashboard']);
      }
      return this.router.createUrlTree(['/dashboard']);
    }

    // Non connecté → login
    localStorage.setItem('redirect_after_login', state.url);
    return this.router.createUrlTree(['/login']);
  }
}
