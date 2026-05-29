import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChecklistRequest {
  temperature:   number;
  precipitation: number;
  wind_speed:    number;
  humidity:      number;
  difficulte:    number;
}

export interface ChecklistResponse {
  success:         boolean;
  checklist_item:  string;
  confidence:      number;
  details:         string;
  alert_level:     string;
  recommendations: string[];
  error?:          string;
}

export interface WeatherDTO {
  city:          string;
  date:          string;
  temperature:   number;
  precipitation: number;
  windSpeed:     number;
  humidity:      number;
}

@Injectable({ providedIn: 'root' })
export class ChecklistService {
  private readonly BASE = 'http://localhost:8087/api';

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  // ✅ DÉLÉGUÉ à WeatherService, mais maintenu ici pour compatibilité descendante si besoin
  getWeather(city: string, date: string): Observable<WeatherDTO> {
    const params = new HttpParams().set('city', city).set('date', date);
    return this.http.get<WeatherDTO>(
      `http://localhost:8087/api/weather`,
      { headers: this.headers(), params }
    );
  }

  // Mode MANUEL : POST /api/checklist/predict
  predict(req: ChecklistRequest): Observable<ChecklistResponse> {
    return this.http.post<ChecklistResponse>(
      `${this.BASE}/checklist/predict`,
      req,
      { headers: this.headers() }
    );
  }

  // ✅ CORRIGÉ : POST /api/checklist/recommandation (body JSON, pas GET)
  recommandationAuto(city: string, date: string, difficulte: number): Observable<ChecklistResponse> {
    const body = { city, date, difficulte };
    return this.http.post<ChecklistResponse>(
      `${this.BASE}/checklist/recommandation`,
      body,
      { headers: this.headers() }
    );
  }
}
