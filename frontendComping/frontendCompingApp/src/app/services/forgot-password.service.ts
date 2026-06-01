import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ForgotPasswordService {
  private apiUrl = 'http://localhost:8087/api/auth/forgot-password'; 

  constructor(private http: HttpClient) {}

  forgotPassword(email: string): Observable<ForgotPasswordResponse> {
    const dto: ForgotPasswordRequest = { email };
    return this.http.post<ForgotPasswordResponse>(this.apiUrl, dto, {
      headers: { 'Content-Type': 'application/json' }
    });
  }
}
