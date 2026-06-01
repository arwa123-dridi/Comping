import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = 'http://localhost:8087/api';

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${localStorage.getItem('authToken') ?? ''}`,
      'Content-Type': 'application/json'
    });
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users`, { headers: this.headers() });
  }

  // ✅ Utilise l'endpoint /count (plus efficace)
  getTotalUsers(): Observable<number> {
    return this.http.get<number>(`${this.base}/users/count`, { headers: this.headers() });
  }

  updateUserStatus(id: string, actif: boolean): Observable<any> {
    return this.http.patch(
      `${this.base}/users/${id}/status`,
      { statut: actif, actif: actif },
      { headers: this.headers() }
    );
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`, {
      headers: this.headers(),
      responseType: 'text' as 'json'
    });
  }

  getUserById(id: string): Observable<any> {
    return this.http.get<any>(`${this.base}/users/${id}`, { headers: this.headers() });
  }

  updateUser(id: string, data: Partial<User>): Observable<any> {
    return this.http.put<any>(`${this.base}/users/${id}`, data, { headers: this.headers() });
  }

  updatePassword(id: string, payload: { currentPassword: string; newPassword: string }): Observable<any> {
    return this.http.patch<any>(`${this.base}/users/${id}/password`, payload, { headers: this.headers() });
  }
}
