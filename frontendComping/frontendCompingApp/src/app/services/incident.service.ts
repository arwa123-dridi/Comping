import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export interface IncidentRequest {
  type: string;
  statut: string;
  descrition: string;
  dateDeclaration: Date;
  resolu: boolean;
}

export interface IncidentResponse {
  idIncident: string;
  type: string;
  statut: string;
  descrition: string;
  dateDeclaration: Date;
  resolu: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class IncidentService {
  private readonly baseUrl = 'http://localhost:8087/api/incidents';

  constructor(private http: HttpClient) {}

  createIncident(payload: IncidentRequest): Observable<IncidentResponse> {
    return this.http.post<IncidentResponse>(this.baseUrl, payload);
  }

  getIncidents(): Observable<IncidentResponse[]> {
    return this.http.get<IncidentResponse[]>(this.baseUrl);
  }

  getIncidentById(id: string): Observable<IncidentResponse> {
    return this.http.get<IncidentResponse>(`${this.baseUrl}/${id}`);
  }

  updateIncident(id: string, payload: IncidentRequest): Observable<IncidentResponse> {
    return this.http.put<IncidentResponse>(`${this.baseUrl}/${id}`, payload);
  }

  deleteIncident(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  resolveIncident(id: string): Observable<IncidentResponse> {
    return this.http.get<IncidentResponse>(`${this.baseUrl}/${id}`).pipe(
      switchMap(incident => {
        const payload: IncidentRequest = {
          ...incident,
          resolu: true,
          statut: 'RESOLU'
        };
        return this.http.put<IncidentResponse>(`${this.baseUrl}/${id}`, payload);
      })
    );
  }
}
