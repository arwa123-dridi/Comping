// src/app/services/checklist.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// ─── DTOs (alignés sur ChecklistRequest / ChecklistResponse Spring Boot) ──────

export interface ChecklistRequest {
  temperature:   number;
  precipitation: number;
  wind_speed:    number;
  humidity:      number;
  difficulte:    number;
}

export interface ChecklistResponse {
  success:        boolean;
  checklist_item: string;
  confidence:     number;   // 0.0 – 1.0
  details:        string;
  alert_level:    string;   // ex: "VERT — Conditions favorables"
  recommendations: string[];
  error?:         string;
}

export interface WeatherDTO {
  city:          string;
  date:          string;
  temperature:   number;
  precipitation: number;
  windSpeed:     number;
  humidity:      number;
}

export interface WeatherRequest {
  city: string;
  date: string; // yyyy-MM-dd
}

// ─── Service ─────────────────────────────────────────────────────────────────

@Injectable({ providedIn: 'root' })
export class ChecklistService {

  /** Spring Boot — JAMAIS Flask directement */
  private readonly BASE = 'http://localhost:8087/api';

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  // ── Mode MANUEL : POST /api/checklist/predict ─────────────────────────────
  predict(req: ChecklistRequest): Observable<ChecklistResponse> {
    return this.http.post<ChecklistResponse>(
      `${this.BASE}/checklist/predict`,
      req,
      { headers: this.headers() }
    );
  }

  // ── Mode AUTO : GET /api/checklist/recommandation?city&date&difficulte ─────
  recommandationAuto(
    city: string,
    date: string,
    difficulte: number
  ): Observable<ChecklistResponse> {
    const params = new HttpParams()
      .set('city', city)
      .set('date', date)
      .set('difficulte', difficulte.toString());

    return this.http.get<ChecklistResponse>(
      `${this.BASE}/checklist/recommandation`,
      { headers: this.headers(), params }
    );
  }

  // ── Météo preview : POST /api/weather ────────────────────────────────────
  getWeather(city: string, date: string): Observable<WeatherDTO> {
    const body: WeatherRequest = { city, date };
    return this.http.post<WeatherDTO>(
      `${this.BASE}/weather`,
      body,
      { headers: this.headers() }
    );
  }
}