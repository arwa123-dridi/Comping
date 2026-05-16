// src/app/services/signin.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginDTORequest { 
  email: string; 
  password: string; 
}

export interface LoginDTOResponse { 
  token: string; 
  username?: string;
  roles?: string[];
}

@Injectable({ providedIn: 'root' })
export class SigninService {

  private readonly API = 'http://localhost:8087/api/auth/login';

  constructor(private http: HttpClient) {}

  login(dto: LoginDTORequest): Observable<LoginDTOResponse> {
    return this.http.post<LoginDTOResponse>(this.API, dto, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(tap(res => this.saveSession(res, dto.email)));
  }

  saveSession(res: LoginDTOResponse, loginEmail = ''): void {
    if (!res?.token) return;

    const payload   = this.decodeJwt(res.token);
    const userId    = payload?.['id']        ?? '';
    const userRole  = payload?.['role']      ?? 'USER';
    const userEmail = payload?.['sub']       ?? loginEmail;
    // Certains JWT incluent firstName/lastName dans les claims
    const firstName = payload?.['firstName'] ?? payload?.['prenom'] ?? '';
    const lastName  = payload?.['lastName']  ?? payload?.['nom']    ?? '';
    // userNom = nom complet si dispo, sinon partie de l'email avant @
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

    console.log('[Auth] userId:', userId, '| role:', userRole, '| nom:', userNom);
  }

  decodeJwt(token: string): Record<string, any> | null {
    try {
      const parts  = token.split('.');
      if (parts.length !== 3) return null;
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '=='.slice(0, (4 - base64.length % 4) % 4);
      return JSON.parse(atob(padded));
    } catch { 
      return null; 
    }
  }

  logout(): void {
    ['authToken', 'userId', 'userEmail', 'userRole', 'userNom', 'userPrenom', 'tokenExpiry']
      .forEach(k => localStorage.removeItem(k));
  }

  getToken(): string | null { 
    return localStorage.getItem('authToken'); 
  }

  saveToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  getUserId(): string | null { 
    return localStorage.getItem('userId'); 
  }

  getRole(): string | null { 
    return localStorage.getItem('userRole'); 
  }

  isConnected(): boolean { 
    return !!this.getToken(); 
  }

  isOrganisateur(): boolean {
    const r = this.getRole() ?? '';
    return r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }

  isAdmin(): boolean {
    const r = this.getRole() ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }
}