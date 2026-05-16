import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ParticipationDTO } from '../models/participation.model';

@Injectable({ providedIn: 'root' })
export class ParticipationService {
  private apiUrl = 'http://localhost:8087/api/participations';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  getMyParticipations(): Observable<ParticipationDTO[]> {
    const userId = localStorage.getItem('userId');
    return this.http.get<ParticipationDTO[]>(`${this.apiUrl}/utilisateur/${userId}`, {
      headers: this.getHeaders()
    });
  }

  deleteParticipation(sortieId: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/sortie/${sortieId}/utilisateur/${userId}`, {
      headers: this.getHeaders()
    });
  }
}