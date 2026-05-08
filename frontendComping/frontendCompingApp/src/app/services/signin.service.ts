// src/app/services/signin.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginDTORequest {
  email:    string;
  password: string;
}

export interface LoginDTOResponse {
  id:        any;
  userId:    any;
  role:      string;
  roles:     string[];
  email:     string;
  lastName:  string;
  firstName: string;
  token:     string;
  username:  string;
}

@Injectable({ providedIn: 'root' })
export class SigninService {

  private apiUrl = 'http://localhost:8087/api/auth/login';

  constructor(private http: HttpClient) {}

  login(dto: LoginDTORequest): Observable<LoginDTOResponse> {
    return this.http.post<LoginDTOResponse>(this.apiUrl, dto, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      tap(res => this.saveSession(res))
    );
  }

  /**  Sauvegarde TOUT en localStorage — gardes peuvent maintenant lire userRole */
  saveSession(res: LoginDTOResponse): void {
    const role = res.role
      ?? res.roles?.[0]
      ?? 'USER';

    localStorage.setItem('authToken',   res.token);
    localStorage.setItem('userId',      String(res.userId ?? res.id));
    localStorage.setItem('userEmail',   res.email);
    localStorage.setItem('userRole',    role);           // ← MANQUAIT
    localStorage.setItem('userPrenom',  res.firstName);
    localStorage.setItem('userNom',     res.lastName);

    // Expiration token : 24h
    const expiry = new Date();
    expiry.setHours(expiry.getHours() + 24);
    localStorage.setItem('tokenExpiry', expiry.toISOString());
  }

  logout(): void {
    ['authToken','userId','userEmail','userRole','userPrenom','userNom','tokenExpiry']
      .forEach(k => localStorage.removeItem(k));
  }

  getToken():  string | null { return localStorage.getItem('authToken'); }
  getUserId(): string | null { return localStorage.getItem('userId'); }
  getRole():   string | null { return localStorage.getItem('userRole'); }

  isConnected():    boolean { return !!this.getToken(); }
  isOrganisateur(): boolean { return this.getRole() === 'ORGANISATEUR'; }
  isAdmin():        boolean { return this.getRole() === 'ADMIN'; }
  isUser():         boolean {
    const r = this.getRole();
    return r === 'USER' || r === 'ROLE_USER';
  }
}