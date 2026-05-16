import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class PaymentEvent {
    private apiUrl = 'http://localhost:8087';
     constructor(private http: HttpClient) {}

  checkout(eventId: string): Observable<string> {
     const token = localStorage.getItem('authToken');
    return this.http.post(
      `${this.apiUrl}/paymentEvent/checkout/${eventId}`,
      {},
      { responseType: 'text' ,

         headers: {
        Authorization: `Bearer ${token}`
      }
      } 
    );
  }
  
}
