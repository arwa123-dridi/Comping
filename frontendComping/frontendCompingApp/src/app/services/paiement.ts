import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PaiementResponse {
  id: string;
  montant: number;
  datePaiement: string;
  statut: string;
  methode: string;
  reservationId: string;
  stripeClientSecret: string;
}

@Injectable({ providedIn: 'root' })
export class PaiementService {

  private api = 'http://localhost:8080/api/paiements';

  constructor(private http: HttpClient) {}

  createPaiement(reservationId: string, montant: number): Observable<PaiementResponse> {
    return this.http.post<PaiementResponse>(`${this.api}/create/${reservationId}`, {
      montant: montant,
      methode: 'CARTE'
    });
  }

  validerPaiement(paiementId: string): Observable<PaiementResponse> {
    return this.http.post<PaiementResponse>(`${this.api}/valider/${paiementId}`, {});
  }

  getByReservation(reservationId: string): Observable<PaiementResponse> {
    return this.http.get<PaiementResponse>(`${this.api}/reservation/${reservationId}`);
  }

  getAll(): Observable<PaiementResponse[]> {
    return this.http.get<PaiementResponse[]>(this.api);
  }
}