import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
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

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/users`, { headers: this.headers() });
  }

  getTotalUsers(): Observable<number> {
    return this.getAllUsers().pipe(map(users => users.length));
  }

  updateUserStatus(id: string, actif: boolean): Observable<any> {
    return this.http.patch(
      `${this.base}/users/${id}/status`,
      { statut: actif, actif: actif },
      { headers: this.headers() }
    );
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`, { headers: this.headers() });
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