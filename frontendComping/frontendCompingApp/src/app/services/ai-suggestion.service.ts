import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PostDraft {
  title: string;
  content: string;
  hashtags: string[];
}

@Injectable({ providedIn: 'root' })
export class AiSuggestionService {

  private readonly base = 'http://localhost:8087/api/ai/suggestions';

  constructor(private http: HttpClient) {}

  /** Étape 1 : récupère 3 sujets basés sur les tendances camping réelles. */
  getSuggestedTopics(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/topics`, { headers: this.authHeaders() });
  }

  /** Étape 2 : génère un post complet pour le sujet choisi. */
  generatePost(topic: string): Observable<PostDraft> {
    return this.http.post<PostDraft>(`${this.base}/generate`, { topic }, { headers: this.authHeaders() });
  }

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
