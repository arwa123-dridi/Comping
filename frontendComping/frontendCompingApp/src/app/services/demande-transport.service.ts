import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  DemandeTransportRequest,
  DemandeTransportResponse,
  TraitementDemandeRequest,
  CreneauSuggestion
} from '../models/demande-transport.model';

@Injectable({ providedIn: 'root' })
export class DemandeTransportService {
  private apiUrl = 'http://localhost:8087/api/demandes-transport';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  create(dto: DemandeTransportRequest): Observable<DemandeTransportResponse> {
    return this.http.post<DemandeTransportResponse>(this.apiUrl, dto, { headers: this.getHeaders() });
  }

  getMine(): Observable<DemandeTransportResponse[]> {
    return this.http.get<DemandeTransportResponse[]>(`${this.apiUrl}/me`, { headers: this.getHeaders() });
  }

  getById(id: string): Observable<DemandeTransportResponse> {
    return this.http.get<DemandeTransportResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  getAll(): Observable<DemandeTransportResponse[]> {
    return this.http.get<DemandeTransportResponse[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  update(id: string, dto: DemandeTransportRequest): Observable<DemandeTransportResponse> {
    return this.http.put<DemandeTransportResponse>(`${this.apiUrl}/${id}`, dto, { headers: this.getHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  traiter(id: string, dto: TraitementDemandeRequest): Observable<DemandeTransportResponse> {
    return this.http.patch<DemandeTransportResponse>(`${this.apiUrl}/${id}/traitement`, dto, { headers: this.getHeaders() });
  }

  suggestionCreneau(id: string): Observable<CreneauSuggestion> {
    return this.http.get<CreneauSuggestion>(`${this.apiUrl}/${id}/suggestion-creneau`, { headers: this.getHeaders() });
  }
}
