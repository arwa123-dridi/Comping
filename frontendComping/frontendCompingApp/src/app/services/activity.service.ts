import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {

  private baseUrl = 'http://localhost:8087/api/activities';

  constructor(private http: HttpClient) { }

 getAllActivities(): Observable<any[]> {
  const token = localStorage.getItem('authToken');

  return this.http.get<any[]>(`${this.baseUrl}/GetAllActivities`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}
}