import { Injectable } from '@angular/core';
import { AiRecommendation } from '../models/ai-recommendationEvent.model';
import { Observable } from 'rxjs/internal/Observable';
import { HttpClient } from '@angular/common/http';
import { UserProfileEvent } from '../models/UserPrrofileEvent.model';

@Injectable({
  providedIn: 'root',
})
export class AiRecommendationEvent {
  private apiUrl = 'http://localhost:8087/api/ai';

  constructor(private http: HttpClient) {}

  recommendEvents(userProfile: UserProfileEvent): Observable<AiRecommendation> {
        const token = localStorage.getItem('authToken');
    return this.http.post<AiRecommendation>(
      `${this.apiUrl}/recommend-events`,
      userProfile,
      {
        headers: token
          ? { Authorization: `Bearer ${token}` }
          : {}
      }
    );
  }
}
