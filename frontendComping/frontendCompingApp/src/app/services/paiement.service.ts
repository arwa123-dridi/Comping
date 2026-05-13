import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PaiementService {
  private apiUrl = 'http://localhost:8087/api/paiements';

  constructor(private http: HttpClient) {}

  createPaiement(reservationId: string, montant: number, methode: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/create/${reservationId}`, { montant, methode });
  }

  validerPaiement(paiementId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/valider/${paiementId}`, {});
  }

  getByReservation(reservationId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/reservation/${reservationId}`);
  }
}