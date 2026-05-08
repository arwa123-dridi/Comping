// src/app/services/planning.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SortieResponse } from '../models/sortie.model';

// ─── DTOs alignés sur le backend ────────────────────────────────────────────

export interface SortiePlanifieeDTO {
  sortie:               SortieResponse;
  dateRecommandee:      string;          // "2026-06-15"
  scoreMatch:           number;          // 0–100
  raisonsRecommandation: string[];
  estMeilleurChoix:     boolean;
  placesRestantes:      number;
  saison:               string;
}

// ─── Service ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class PlanningService {

  private readonly BASE = 'http://localhost:8087/api/planning';

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  /** Planning complet 3 mois */
  getPlanning(userId: string): Observable<SortiePlanifieeDTO[]> {
    return this.http.get<SortiePlanifieeDTO[]>(`${this.BASE}/${userId}`, {
      headers: this.headers()
    });
  }

  /** Planning filtré par mois (ex: "2026-06") */
  getPlanningParMois(userId: string, mois: string): Observable<SortiePlanifieeDTO[]> {
    const params = new HttpParams().set('mois', mois);
    return this.http.get<SortiePlanifieeDTO[]>(`${this.BASE}/${userId}/calendrier`, {
      headers: this.headers(), params
    });
  }

  /** Confirmer une sortie → inscription + MAJ profil IA */
  validerSortie(userId: string, sortieId: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.BASE}/${userId}/valider`,
      { sortieId },
      { headers: this.headers() }
    );
  }
}