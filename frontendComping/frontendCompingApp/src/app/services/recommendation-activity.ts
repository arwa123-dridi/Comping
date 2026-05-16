import { HttpClient ,HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/internal/Observable';

@Injectable({
  providedIn: 'root',
})
export class RecommendationActivity {
   private apiUrl =
    'http://localhost:8087/api/recommendationActivity';

  constructor(private http: HttpClient) {}

  suggestActivities(event: any): Observable<any[]> {
 const token = localStorage.getItem('authToken');

  return this.http.post<any[]>(
    `${this.apiUrl}/suggest-activities`,
    event,
    {
      headers: token
        ? { Authorization: `Bearer ${token}` }
        : {}
    }
  );
  
}

}