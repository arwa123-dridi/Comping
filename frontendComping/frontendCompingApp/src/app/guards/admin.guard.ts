// src/app/guards/admin.guard.ts

import { Injectable } from '@angular/core';
import {
  CanActivate,
  Router,
  UrlTree,
  ActivatedRouteSnapshot,
  RouterStateSnapshot
} from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId');

    const isAdmin = userRole === 'ADMIN' || userRole === 'ROLE_ADMIN';

    // Autorise seulement ADMIN / ROLE_ADMIN
    if (token && userId && isAdmin) {
      return true;
    }

    // Sinon -> /dashboard
    if (token && userId) {
      return this.router.createUrlTree(['/dashboard'], {
        queryParams: { error: 'unauthorized' }
      });
    }

    // Non connecté - rediriger vers login
    localStorage.setItem('redirect_after_login', state.url);
    return this.router.createUrlTree(['/login']);
  }
}
