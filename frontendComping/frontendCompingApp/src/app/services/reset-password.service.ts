import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ResetPasswordRequest {
  token: string;
  nouveauMotDePasse: string;
}

export interface ResetPasswordResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ResetPasswordService {
  private apiUrl = 'http://localhost:8087/api/auth/reset-password';

  constructor(private http: HttpClient) {}

  resetPassword(token: string, newPassword: string): Observable<ResetPasswordResponse> {
    const dto: ResetPasswordRequest = { 
      token, 
      nouveauMotDePasse: newPassword 
    };
    return this.http.post<ResetPasswordResponse>(this.apiUrl, dto, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    });
  }
}

