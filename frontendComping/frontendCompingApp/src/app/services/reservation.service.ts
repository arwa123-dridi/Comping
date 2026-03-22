import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  constructor(private api: ApiService) { }

  getUserReservations(): Observable<any[]> {
    return this.api.get('reservations/user');
  }

  createReservation(reservationData: any): Observable<any> {
    return this.api.post('reservations', reservationData);
  }

  getReservation(id: string): Observable<any> {
    return this.api.get(`reservations/${id}`);
  }
}
