import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RecommendationService {

  private api = 'http://localhost:8087/api/recommendations';

  constructor(private http: HttpClient) {}

  getRecommendations(productIds: string[]): Observable<string[]> {
    return this.http.post<string[]>(
      `${this.api}/getRecommendation`,
      productIds
    );
  }
}