import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreneauLivraison, CreneauLivraisonRequest } from '../models/creneau-livraison.model';

@Injectable({ providedIn: 'root' })
export class CreneauLivraisonService {
  private apiUrl = 'http://localhost:8087/api/creneaux-livraison';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAll(): Observable<CreneauLivraison[]> {
    return this.http.get<CreneauLivraison[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getById(id: string): Observable<CreneauLivraison> {
    return this.http.get<CreneauLivraison>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  create(dto: CreneauLivraisonRequest): Observable<CreneauLivraison> {
    return this.http.post<CreneauLivraison>(this.apiUrl, dto, { headers: this.getHeaders() });
  }

  update(id: string, dto: CreneauLivraisonRequest): Observable<CreneauLivraison> {
    return this.http.put<CreneauLivraison>(`${this.apiUrl}/${id}`, dto, { headers: this.getHeaders() });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
}
