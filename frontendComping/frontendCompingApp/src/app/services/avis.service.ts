import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvisRequest, AvisResponse } from '../models/avis.model';

@Injectable({ providedIn: 'root' })
export class AvisService {
  private apiUrl = 'http://localhost:8087/api/avis';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  creerAvis(dto: AvisRequest): Observable<AvisResponse> {
    return this.http.post<AvisResponse>(this.apiUrl, dto, { headers: this.getHeaders() });
  }

  getByCible(cibleId: string, typeCible: string): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(`${this.apiUrl}/cible/${cibleId}?typeCible=${typeCible}`, { headers: this.getHeaders() });
  }
}
