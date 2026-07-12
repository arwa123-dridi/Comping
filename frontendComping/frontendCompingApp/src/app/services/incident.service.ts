import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncidentRequest, IncidentResponse, TraitementIncidentRequest } from '../models/incident.model';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private apiUrl = 'http://localhost:8087/api/incidents';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  create(dto: IncidentRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(this.apiUrl, dto, { headers: this.getHeaders() });
  }

  getMine(): Observable<IncidentResponse[]> {
    return this.http.get<IncidentResponse[]>(`${this.apiUrl}/me`, { headers: this.getHeaders() });
  }

  getById(id: string): Observable<IncidentResponse> {
    return this.http.get<IncidentResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  getAll(): Observable<IncidentResponse[]> {
    return this.http.get<IncidentResponse[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  update(id: string, dto: IncidentRequest): Observable<IncidentResponse> {
    return this.http.put<IncidentResponse>(`${this.apiUrl}/${id}`, dto, { headers: this.getHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  traiter(id: string, dto: TraitementIncidentRequest): Observable<IncidentResponse> {
    return this.http.patch<IncidentResponse>(`${this.apiUrl}/${id}/traitement`, dto, { headers: this.getHeaders() });
  }

  reclassifier(id: string): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(`${this.apiUrl}/${id}/reclassifier`, {}, { headers: this.getHeaders() });
  }
}
