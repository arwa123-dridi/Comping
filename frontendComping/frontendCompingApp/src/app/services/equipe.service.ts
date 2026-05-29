import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EquipeRequest, EquipeResponse } from '../models/equipe.model';
import { EquipeScoreDTO } from '../models/equipe-score.model';

@Injectable({ providedIn: 'root' })
export class EquipeService {
  private apiUrl = 'http://localhost:8087/api/equipes';
  private recommandationsUrl = 'http://localhost:8087/api/recommandations';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllEquipes(): Observable<EquipeResponse[]> {
    return this.http.get<EquipeResponse[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getEquipeById(id: string): Observable<EquipeResponse> {
    return this.http.get<EquipeResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  createEquipe(equipe: EquipeRequest): Observable<EquipeResponse> {
    return this.http.post<EquipeResponse>(this.apiUrl, equipe, { headers: this.getHeaders() });
  }

  updateEquipe(id: string, equipe: EquipeRequest): Observable<EquipeResponse> {
    return this.http.put<EquipeResponse>(`${this.apiUrl}/${id}`, equipe, { headers: this.getHeaders() });
  }

  deleteEquipe(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  getEquipesAvecPlace(): Observable<EquipeResponse[]> {
    return this.http.get<EquipeResponse[]>(`${this.apiUrl}/avec-place`, { headers: this.getHeaders() });
  }

  // ✅ CORRIGÉ : body JSON au lieu de query param
  ajouterMembre(equipeId: string, userId: string, userNom: string): Observable<EquipeResponse> {
    const body = { utilisateurNom: userNom };
    return this.http.post<EquipeResponse>(
      `${this.apiUrl}/${equipeId}/membres/${userId}`,
      body,
      { headers: this.getHeaders() }
    );
  }

  retirerMembre(equipeId: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${equipeId}/membres/${userId}`, {
      headers: this.getHeaders()
    });
  }

  getRecommandationsEquipes(userId: string): Observable<EquipeScoreDTO[]> {
    return this.http.get<EquipeScoreDTO[]>(
      `${this.recommandationsUrl}/equipes?userId=${userId}`,
      { headers: this.getHeaders() }
    );
  }
}
