import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrganizerGuard implements CanActivate {
  
  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');
    const userId = localStorage.getItem('userId');
    
    // Check if logged in and ORGANISATEUR
    if (token && userId && userRole === 'ORGANISATEUR') {
      return true;
    }
    
    // Logged but not organizer → user dashboard
    if (token && userId && userRole !== 'ORGANISATEUR') {
      this.router.navigate(['/dashboard'], { 
        queryParams: { error: 'requires_organizer' }
      });
      return false;
    }
    
    // Not logged → login
    this.router.navigate(['/signin'], { 
      queryParams: { returnUrl: state.url, requiredRole: 'ORGANISATEUR' }
    });
    return false;
  }
}
