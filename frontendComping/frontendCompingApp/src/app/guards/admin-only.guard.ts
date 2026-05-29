import { Injectable } from '@angular/core';
import {
  CanActivate, ActivatedRouteSnapshot,
  RouterStateSnapshot, Router, UrlTree
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AdminOnlyGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    const token    = localStorage.getItem('authToken');
    const userId   = localStorage.getItem('userId');
    const userRole = localStorage.getItem('userRole');

    const isAdmin  = userRole === 'ADMIN' || userRole === 'ROLE_ADMIN';

    // Autorise seulement ADMIN
    if (token && userId && isAdmin) {
      return true;
    }

    // Sinon → /dashboard
    if (token && userId) {
      return this.router.createUrlTree(['/dashboard'], {
        queryParams: { error: 'unauthorized' }
      });
    }

    // Non connecté → login
    localStorage.setItem('redirect_after_login', state.url);
    return this.router.createUrlTree(['/login']);
  }
}
