import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LoginDTORequest {
  username: string;
  password: string;
}

export interface LoginDTOResponse {
  token: string;
  username: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class SigninService {

 private apiUrl = 'http://localhost:8087/api/auth/login'; 

  constructor(private http: HttpClient,) {}

login(dto: { email: string, password: string }): Observable<LoginDTOResponse> {
  return this.http.post<LoginDTOResponse>(this.apiUrl, dto, {
    headers: { 'Content-Type': 'application/json' }
  });
}
  saveToken(token: string) {
    localStorage.setItem('authToken', token);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

}
