import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
 private baseUrl = 'http://localhost:8087'; 
  constructor(private http: HttpClient) { }
   getEvents(): Observable<any> {
    return this.http.get(`${this.baseUrl}/events`);
  }
}
