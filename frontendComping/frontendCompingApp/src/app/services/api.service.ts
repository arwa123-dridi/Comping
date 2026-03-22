import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    });
  }

  get<T = any>(endpoint: string): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}/${endpoint}`, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  post<T = any>(endpoint: string, body?: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, body, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  put<T = any>(endpoint: string, body?: any): Observable<T> {
    return this.http.put<T>(`${this.baseUrl}/${endpoint}`, body, { headers: this.getAuthHeaders() })
      .pipe(
        catchError(this.handleError)
      );
  }

  getEvents(): Observable<any> {
    return this.get('events');
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur inattendue s\'est produite';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Erreur: ${error.error.message}`;
    } else {
      errorMessage = `Code: ${error.status}\nMessage: ${error.message}`;
    }
    
    console.error('API Error:', error);
    return throwError(() => ({ message: errorMessage }));
  }
}
