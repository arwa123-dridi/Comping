import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

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
 private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());

  constructor(private http: HttpClient) {}

  private hasToken(): boolean {
    return !!localStorage.getItem('authToken');
  }

  isLoggedIn$(): Observable<boolean> {
    return this.isLoggedInSubject.asObservable();
  }

login(dto: { email: string, password: string }): Observable<LoginDTOResponse> {
  return this.http.post<LoginDTOResponse>(this.apiUrl, dto, {
    headers: { 'Content-Type': 'application/json' }
  });
}
  saveToken(token: string) {
    localStorage.setItem('authToken', token);
    this.isLoggedInSubject.next(true);
  }

  logout() {
    localStorage.removeItem('authToken');
    this.isLoggedInSubject.next(false);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isLoggedIn(): boolean {
    return this.hasToken();
  }

}
