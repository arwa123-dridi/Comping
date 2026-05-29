import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { SortieRequest, SortieResponse } from '../models/sortie.model';
import { ParticipationDTO } from '../models/participation.model';
import { SortieScoreDTO } from '../models/sortie-score.model';

@Injectable({ providedIn: 'root' })
export class SortieService {
  private apiUrl = 'http://localhost:8087/api/sorties';
  private recommandationsUrl = 'http://localhost:8087/api/recommandations';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  getAllSorties(): Observable<SortieResponse[]> {
    return this.http.get<SortieResponse[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getSortieById(id: string): Observable<SortieResponse> {
    return this.http.get<SortieResponse>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  createSortie(sortie: SortieRequest): Observable<SortieResponse> {
    return this.http.post<SortieResponse>(this.apiUrl, sortie, { headers: this.getHeaders() });
  }

  updateSortie(id: string, sortie: SortieRequest): Observable<SortieResponse> {
    return this.http.put<SortieResponse>(`${this.apiUrl}/${id}`, sortie, { headers: this.getHeaders() });
  }

  deleteSortie(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  getParticipants(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/participants`, { headers: this.getHeaders() });
  }

  getProchainesSorties(): Observable<SortieResponse[]> {
    return this.http.get<SortieResponse[]>(`${this.apiUrl}/prochaines`, { headers: this.getHeaders() });
  }

  // ✅ CORRIGÉ : body JSON au lieu de query params (sécurité)
  inscrire(sortieId: string): Observable<ParticipationDTO> {
    const userId    = localStorage.getItem('userId') ?? '';
    const userNom   = localStorage.getItem('userNom') ?? '';
    const userEmail = localStorage.getItem('userEmail') ?? '';

    if (!userId || userId === 'undefined' || userId === 'null') {
      return throwError(() => new Error('Utilisateur non connecté'));
    }

    const body = { utilisateurId: userId, utilisateurNom: userNom, utilisateurEmail: userEmail };
    return this.http.post<ParticipationDTO>(
      `${this.apiUrl}/${sortieId}/inscription`,
      body,
      { headers: this.getHeaders() }
    );
  }

  desinscrire(sortieId: string): Observable<void> {
    const userId = localStorage.getItem('userId') ?? '';
    return this.http.delete<void>(
      `${this.apiUrl}/${sortieId}/inscription/${userId}`,
      { headers: this.getHeaders() }
    );
  }

  getRecommandations(userId: string): Observable<SortieScoreDTO[]> {
    return this.http.get<SortieScoreDTO[]>(
      `${this.recommandationsUrl}/sorties?userId=${userId}`,
      { headers: this.getHeaders() }
    );
  }

  getRecommandationsLocales(userId: string, sorties: SortieResponse[]): SortieResponse[] {
    const participated = sorties.filter(s =>
      (s.participantIds ?? []).map(String).includes(String(userId))
    );
    if (participated.length === 0) {
      return sorties.filter(s => s.difficulte === 'FACILE' && (s.placesDisponibles ?? 1) > 0).slice(0, 4);
    }
    const freq: Record<string, number> = {};
    participated.forEach(s => freq[s.difficulte] = (freq[s.difficulte] || 0) + 1);
    const topDiff = Object.entries(freq).sort((a, b) => b[1] - a[1])[0][0];
    return sorties
      .filter(s => !(s.participantIds ?? []).map(String).includes(String(userId)) && (s.placesDisponibles ?? 1) > 0)
      .sort((a, b) => (b.difficulte === topDiff ? 1 : 0) - (a.difficulte === topDiff ? 1 : 0))
      .slice(0, 4);
  }
}
