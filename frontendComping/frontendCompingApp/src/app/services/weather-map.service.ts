import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, forkJoin, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { Urgence } from './urgence-advanced.service';
import { Securite } from './securite.service';

export interface WeatherData {
  city: string;
  date: string;
  temperature: number;
  precipitation: number;
  windSpeed: number;
  humidity: number;
  conditions?: string;
}

export interface MapLocation {
  lat: number;
  lng: number;
  title: string;
  type: 'urgence' | 'securite' | 'alerte';
  data: any;
}

export interface RiskAssessment {
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  factors: string[];
  recommendations: string[];
  weatherImpact: number;
}

@Injectable({
  providedIn: 'root'
})
export class WeatherMapService {
  private readonly weatherUrl = 'http://localhost:8087/api/weather';
  private mapLocationsSubject = new BehaviorSubject<MapLocation[]>([]);
  public mapLocations$ = this.mapLocationsSubject.asObservable();

  private centerLocationSubject = new BehaviorSubject<{ lat: number; lng: number }>({
    lat: 36.8065,
    lng: 10.1815
  });
  public centerLocation$ = this.centerLocationSubject.asObservable();

  constructor(private http: HttpClient) {}

  getWeather(city: string, date: string): Observable<WeatherData> {
    return this.http.post<WeatherData>(this.weatherUrl, { city, date });
  }

  getWeatherForLocation(lat: number, lng: number, date: string): Observable<WeatherData> {
    return this.http.post<WeatherData>(`${this.weatherUrl}/coords`, { lat, lng, date });
  }

  updateMapLocations(urgences: Urgence[], securites: Securite[]): void {
    const locations: MapLocation[] = [];

    urgences.forEach(u => {
      if (u.latitude && u.longitude) {
        locations.push({
          lat: u.latitude,
          lng: u.longitude,
          title: u.titre,
          type: 'urgence',
          data: u
        });
      }
    });

    securites.forEach(s => {
      if (s.monitoringLocations && s.monitoringLocations.length > 0) {
        locations.push({
          lat: this.extractLat(s.monitoringLocations[0]),
          lng: this.extractLng(s.monitoringLocations[0]),
          title: s.titre,
          type: 'securite',
          data: s
        });
      }
    });

    this.mapLocationsSubject.next(locations);
  }

  setCenterLocation(lat: number, lng: number): void {
    this.centerLocationSubject.next({ lat, lng });
  }

  getRiskAssessment(urgence?: Urgence, weather?: WeatherData): Observable<RiskAssessment> {
    const factors: string[] = [];
    const recommendations: string[] = [];
    let weatherImpact = 0;

    if (weather) {
      if (weather.precipitation > 50) {
        factors.push('Heavy precipitation');
        recommendations.push('Avoid outdoor activities');
        weatherImpact += 2;
      }
      if (weather.windSpeed > 50) {
        factors.push('Strong winds');
        recommendations.push('Secure loose equipment');
        weatherImpact += 1;
      }
      if (weather.temperature < 0 || weather.temperature > 40) {
        factors.push('Extreme temperature');
        recommendations.push('Take temperature precautions');
        weatherImpact += 1;
      }
    }

    if (urgence) {
      if (urgence.niveauUrgence === 'IMMEDIATE' || urgence.niveauUrgence === 'TRES_URGENT') {
        factors.push(`${urgence.niveauUrgence} urgency level`);
        weatherImpact += 3;
      }
      if (urgence.impactScore > 7) {
        factors.push('High impact score');
        weatherImpact += 2;
      }
    }

    let level: RiskAssessment['level'] = 'LOW';
    if (weatherImpact >= 6) level = 'CRITICAL';
    else if (weatherImpact >= 4) level = 'HIGH';
    else if (weatherImpact >= 2) level = 'MEDIUM';

    return of({
      level,
      factors,
      recommendations,
      weatherImpact
    });
  }

  getWeatherRisk(weather: WeatherData): Observable<{ risk: string; color: string }> {
    const risk = this.calculateWeatherRisk(weather);
    return of({
      risk,
      color: this.getRiskColor(risk)
    });
  }

  private calculateWeatherRisk(weather: WeatherData): string {
    let score = 0;

    if (weather.precipitation > 70) score += 3;
    else if (weather.precipitation > 30) score += 1;

    if (weather.windSpeed > 60) score += 3;
    else if (weather.windSpeed > 30) score += 1;

    if (weather.temperature < -5 || weather.temperature > 45) score += 3;
    else if (weather.temperature < 0 || weather.temperature > 35) score += 1;

    if (score >= 5) return 'CRITICAL';
    if (score >= 3) return 'HIGH';
    if (score >= 1) return 'MEDIUM';
    return 'LOW';
  }

  private getRiskColor(risk: string): string {
    switch (risk) {
      case 'CRITICAL': return '#dc3545';
      case 'HIGH': return '#fd7e14';
      case 'MEDIUM': return '#ffc107';
      default: return '#28a745';
    }
  }

  private extractLat(location: string): number {
    const parts = location.split(',');
    return parts.length === 2 ? parseFloat(parts[0]) : 0;
  }

  private extractLng(location: string): number {
    const parts = location.split(',');
    return parts.length === 2 ? parseFloat(parts[1]) : 0;
  }

  getUrgenceWeatherImpact(urgence: Urgence): Observable<{ weather?: WeatherData; risk?: RiskAssessment }> {
    if (!urgence.latitude || !urgence.longitude) {
      return of({});
    }

    return this.getWeatherForLocation(urgence.latitude, urgence.longitude, new Date().toISOString().split('T')[0])
      .pipe(
        switchMap(weather => forkJoin([
          of(weather),
          this.getRiskAssessment(urgence, weather)
        ])),
        map(([weather, risk]) => ({ weather, risk })),
        catchError(() => of({}))
      );
  }
}