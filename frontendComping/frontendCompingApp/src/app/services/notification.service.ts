import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationResponse } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = 'http://localhost:8087/api/notifications';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getMine(): Observable<NotificationResponse[]> {
    return this.http.get<NotificationResponse[]>(`${this.apiUrl}/me`, { headers: this.getHeaders() });
  }

  unreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/me/unread-count`, { headers: this.getHeaders() });
  }

  markRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/lu`, {}, { headers: this.getHeaders() });
  }

  markAllRead(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/me/lu`, {}, { headers: this.getHeaders() });
  }
}
