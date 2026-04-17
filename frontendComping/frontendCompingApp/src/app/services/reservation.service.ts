import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private apiUrl = 'http://localhost:8087/api/reservations';  
  constructor(private http: HttpClient) {}

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  getById(id: string): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.apiUrl}/${id}`);
  }

  getHistorique(utilisateurId: string): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/historique/${utilisateurId}`);
  }

  create(request: Partial<Reservation>): Observable<Reservation> {
    return this.http.post<Reservation>(this.apiUrl, request);
  }

  updateStatut(id: string, statut: string): Observable<Reservation> {
    return this.http.patch<Reservation>(`${this.apiUrl}/${id}/statut`, null, {
      params: { statut }
    });
  }

  delete(id: string): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`);
  }
}