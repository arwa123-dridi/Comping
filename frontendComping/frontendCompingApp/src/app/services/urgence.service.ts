import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AlerteRequest {
  siteCampingId: string;
  type: 'INCENDIE' | 'MEDICALE' | 'SECURITE' | 'TECHNIQUE' | 'AUTRE';
  titre: string;
  description: string;
  position: string;
}

export interface AlerteResponse {
  id: string;
  siteCampingId: string;
  type: 'INCENDIE' | 'MEDICALE' | 'SECURITE' | 'TECHNIQUE' | 'AUTRE';
  titre: string;
  description: string;
  dateDeclenchement: string;
  statut: string;
  position: string;
}

@Injectable({
  providedIn: 'root'
})
export class UrgenceService {
  private readonly baseUrl = 'http://localhost:8087/api/alertes';

  constructor(private http: HttpClient) {}

  declencherAlerte(payload: AlerteRequest): Observable<AlerteResponse> {
    return this.http.post<AlerteResponse>(this.baseUrl, payload);
  }

  getAlertes(statut?: string, siteCampingId?: string): Observable<AlerteResponse[]> {
    let params = new HttpParams();

    if (statut && statut.trim()) {
      params = params.set('statut', statut);
    }

    if (siteCampingId && siteCampingId.trim()) {
      params = params.set('siteCampingId', siteCampingId);
    }

    return this.http.get<AlerteResponse[]>(this.baseUrl, { params });
  }

  prendreEnCharge(id: string): Observable<AlerteResponse> {
    return this.http.patch<AlerteResponse>(`${this.baseUrl}/${id}/prendre-en-charge`, {});
  }

  cloturer(id: string): Observable<AlerteResponse> {
    return this.http.patch<AlerteResponse>(`${this.baseUrl}/${id}/cloturer`, {});
  }
}
