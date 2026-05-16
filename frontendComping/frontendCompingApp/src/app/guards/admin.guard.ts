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
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    
    // Récupérer les informations d'authentification
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId');
    
    // Vérifier si l'utilisateur est connecté ET est admin
    if (token && userId && userRole === 'ADMIN') {
      return true; // Admin connecté
    }
    
    // Vérifier si l'utilisateur est connecté mais pas admin
    if (token && userId && userRole !== 'ADMIN') {
      // Rediriger vers le dashboard utilisateur normal
      this.router.navigate(['/dashboard'], { 
        queryParams: { error: 'unauthorized' }
      });
      return false;
    }
    
    // Non connecté - rediriger vers connexion
    this.router.navigate(['/signup'], { 
      queryParams: { returnUrl: state.url, requiredRole: 'ADMIN' }
    });
    return false;
  }
}