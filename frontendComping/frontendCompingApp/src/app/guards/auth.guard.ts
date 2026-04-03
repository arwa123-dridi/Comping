// src/app/guards/auth.guard.ts

import { Injectable } from '@angular/core';
import { 
  CanActivate, 
  ActivatedRouteSnapshot, 
  RouterStateSnapshot, 
  Router,
  UrlTree 
} from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    
    // Récupérer les informations d'authentification
    const token = localStorage.getItem('authToken');
    const userId = localStorage.getItem('userId');
    const userEmail = localStorage.getItem('userEmail');
    
    // Vérifier si l'utilisateur est connecté
    if (token && userId && userEmail) {
      // Optionnel: Vérifier si le token n'est pas expiré
      const tokenExpiry = localStorage.getItem('tokenExpiry');
      if (tokenExpiry && new Date(tokenExpiry) < new Date()) {
        // Token expiré, déconnecter l'utilisateur
        this.clearUserSession();
        this.router.navigate(['/signup'], { 
          queryParams: { returnUrl: state.url, expired: 'true' }
        });
        return false;
      }
      return true; // Utilisateur connecté
    }
    
    // Non connecté - rediriger vers la page de connexion
    // Sauvegarder l'URL pour rediriger après connexion
    this.router.navigate(['/signup'], { 
      queryParams: { returnUrl: state.url }
    });
    return false;
  }

  // Méthode utilitaire pour nettoyer la session
  private clearUserSession(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userNom');
    localStorage.removeItem('userRole');
    localStorage.removeItem('tokenExpiry');
  }
}