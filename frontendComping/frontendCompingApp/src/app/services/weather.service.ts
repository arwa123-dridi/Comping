import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WeatherDTO {
  city:          string;
  date:          string;
  temperature:   number;
  precipitation: number;
  windSpeed:     number;
  humidity:      number;
}

@Injectable({ providedIn: 'root' })
export class WeatherService {
  private readonly apiUrl = 'http://localhost:8087/api/weather';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken') ?? '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getWeather(city: string, date: string): Observable<WeatherDTO> {
    const params = new HttpParams().set('city', city).set('date', date);
    return this.http.get<WeatherDTO>(this.apiUrl, {
      headers: this.getHeaders(),
      params
    });
  }
}
