import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

export interface LoginDTORequest  { email: string; password: string; }
export interface LoginDTOResponse { token: string; }

@Injectable({ providedIn: 'root' })
export class SigninService {
  private readonly API = 'http://localhost:8087/api/auth/login';

  constructor(private http: HttpClient, private router: Router) {}

  login(dto: LoginDTORequest): Observable<LoginDTOResponse> {
    return this.http.post<LoginDTOResponse>(this.API, dto, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(tap(res => {
      this.saveSession(res, dto.email);
      // ✅ AJOUT : redirect vers l'URL stockée avant login (ex: après "Participer")
      this.handleRedirectAfterLogin();
    }));
  }

  // ✅ NOUVEAU : stocke l'URL cible avant de rediriger vers login
  saveRedirectUrl(url: string): void {
    localStorage.setItem('redirect_after_login', url);
  }

  // ✅ NOUVEAU : consomme l'URL et redirige
  private handleRedirectAfterLogin(): void {
    const role = localStorage.getItem('userRole') ?? '';
    const redirect = localStorage.getItem('redirect_after_login');

    if (redirect) {
      localStorage.removeItem('redirect_after_login');
      this.router.navigateByUrl(redirect);
    } else if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
      this.router.navigate(['/admin/dashboard']);
    } else if (role === 'ORGANISATEUR' || role === 'ROLE_ORGANISATEUR') {
      this.router.navigate(['/admin/organizer']);
    } else {
      this.router.navigate(['/Campino']);
    }
  }

  saveSession(res: LoginDTOResponse, loginEmail = ''): void {
    if (!res?.token) return;
    const payload   = this.decodeJwt(res.token);
    const userId    = payload?.['id']        ?? '';
    const userRole  = payload?.['role']      ?? 'USER';
    const userEmail = payload?.['sub']       ?? loginEmail;
    const firstName = payload?.['firstName'] ?? payload?.['prenom'] ?? '';
    const lastName  = payload?.['lastName']  ?? payload?.['nom']    ?? '';
    const userNom   = (firstName || lastName)
      ? `${firstName} ${lastName}`.trim()
      : userEmail.split('@')[0];

    localStorage.setItem('authToken',   res.token);
    localStorage.setItem('userId',      String(userId));
    localStorage.setItem('userEmail',   userEmail);
    localStorage.setItem('userRole',    userRole);
    localStorage.setItem('userNom',     userNom);
    localStorage.setItem('userPrenom',  firstName);

    const expiry = new Date();
    expiry.setHours(expiry.getHours() + 24);
    localStorage.setItem('tokenExpiry', expiry.toISOString());
  }

  decodeJwt(token: string): Record<string, any> | null {
    try {
      const parts  = token.split('.');
      if (parts.length !== 3) return null;
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '=='.slice(0, (4 - base64.length % 4) % 4);
      return JSON.parse(atob(padded));
    } catch { return null; }
  }

  logout(): void {
    ['authToken','userId','userEmail','userRole','userNom','userPrenom','tokenExpiry']
      .forEach(k => localStorage.removeItem(k));
    this.router.navigate(['/login']);
  }

  getToken():       string | null { return localStorage.getItem('authToken'); }
  getUserId():      string | null { return localStorage.getItem('userId'); }
  getRole():        string | null { return localStorage.getItem('userRole'); }
  isConnected():    boolean { return !!this.getToken(); }
  isOrganisateur(): boolean {
    const r = this.getRole() ?? '';
    return r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }
  isAdmin(): boolean {
    const r = this.getRole() ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }
  saveToken(token: string) { localStorage.setItem('authToken', token); }
}
