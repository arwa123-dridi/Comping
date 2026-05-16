import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CarteFedeliteEvent {
 constructor(private http: HttpClient) {}

 getMessage(userId: string) {

    const token = localStorage.getItem('authToken'); 

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.get(
      `http://localhost:8087/carte/message/${userId}`,
      {
        headers,
        responseType: 'text'
      }
    );
  }
}