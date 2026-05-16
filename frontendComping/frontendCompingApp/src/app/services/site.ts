import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SiteService {
  private apiUrl = 'http://localhost:8087/api/sites';

  constructor(private http: HttpClient) { }

  // ── Headers AVEC token (routes protégées) ──────────────────────────
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    if (token) {
      return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    }
    return new HttpHeaders();
  }

  // ── Routes PUBLIQUES (sans token) ──────────────────────────────────

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getDisponibles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/disponibles`);
  }

  getById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getByLocalisation(localisation: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/localisation/${localisation}`);
  }

  filtrer(prixMin?: number, prixMax?: number, disponible?: boolean): Observable<any[]> {
    let params: any = {};
    if (prixMin !== undefined) params['prixMin'] = prixMin;
    if (prixMax !== undefined) params['prixMax'] = prixMax;
    if (disponible !== undefined) params['disponible'] = disponible;
    return this.http.get<any[]>(`${this.apiUrl}/filtrer`, { params });
  }

  // ── Routes PROTÉGÉES (avec token) ──────────────────────────────────

  create(site: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, site, { headers: this.getHeaders() });
  }

  update(id: string, site: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, site, { headers: this.getHeaders() });
  }

  delete(id: string): Observable<any> {
    return this.http.delete(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders(), responseType: 'text' as 'json' }
    );
  }
}