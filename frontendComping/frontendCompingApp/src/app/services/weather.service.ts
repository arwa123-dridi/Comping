import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

export interface GeocodeLocation {
  lat: number;
  lng: number;
  formattedAddress: string;
}

export interface WeatherForecastItem {
  cityName: string;
  date: string;
  temperature: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  description: string;
  iconCode: string;
  minTemperature: number;
  maxTemperature: number;
}

export interface WeatherForecastResponse {
  cityName: string;
  latitude: number;
  longitude: number;
  formattedAddress: string;
  forecast: WeatherForecastItem[];
}

/**
 * Angular service that wraps the Spring Boot weather and location endpoints.
 */
@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private readonly apiBaseUrl = 'http://localhost:8087/api';

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  readonly loading$ = this.loadingSubject.asObservable();

  private readonly errorSubject = new BehaviorSubject<string | null>(null);
  readonly error$ = this.errorSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  /**
   * Resolves a city name into coordinates using the backend geocoding endpoint.
   */
  geocodeAddress(address: string): Observable<GeocodeLocation> {
    return this.trackRequest(
      this.http.get<GeocodeLocation>(`${this.apiBaseUrl}/location/geocode`, {
        params: new HttpParams().set('address', address)
      })
    );
  }

  /**
   * Fetches a forecast for a city.
   */
  getForecastByCity(city: string): Observable<WeatherForecastResponse> {
    return this.trackRequest(
      this.http.get<WeatherForecastResponse>(`${this.apiBaseUrl}/weather/forecast`, {
        params: new HttpParams().set('city', city)
      })
    );
  }

  /**
   * Fetches a forecast for coordinates.
   */
  getForecastByCoordinates(lat: number, lon: number): Observable<WeatherForecastResponse> {
    return this.trackRequest(
      this.http.get<WeatherForecastResponse>(`${this.apiBaseUrl}/weather/forecast`, {
        params: new HttpParams()
          .set('lat', lat.toString())
          .set('lon', lon.toString())
      })
    );
  }

  private trackRequest<T>(request$: Observable<T>): Observable<T> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return request$.pipe(
      finalize(() => this.loadingSubject.next(false)),
      catchError(error => {
        this.errorSubject.next(this.getErrorMessage(error));
        return throwError(() => error);
      })
    );
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null && 'error' in error) {
      const httpError = error as {
        error?: { message?: string; error?: string; detail?: string } | string;
        message?: string;
        statusText?: string;
      };
      if (typeof httpError.error === 'string') {
        return httpError.error;
      }
      if (httpError.error?.message) {
        return httpError.error.message;
      }
      if (httpError.error?.error) {
        return httpError.error.error;
      }
      if (httpError.error?.detail) {
        return httpError.error.detail;
      }
      if (httpError.message) {
        return httpError.message;
      }
      return httpError.statusText || 'Request failed';
    }

    return 'Unexpected error';
  }
}