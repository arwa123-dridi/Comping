import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  message: string;
  response: string;
  context?: string;
  userId?: string;
  timestamp?: Date;
  processingTimeMs?: number;
  success: boolean;
  error?: string;
}

export interface LLMHealth {
  available: boolean;
  modelName: string;
  status: string;
  responseTimeMs: number;
}

@Injectable({
  providedIn: 'root'
})
export class LLMService {

  private readonly apiUrl = 'http://localhost:8087/api/llm';

  constructor(private http: HttpClient) { }

  chat(message: string, context?: string, userId?: string): Observable<ChatMessage> {
    const request = {
      message: message,
      context: context || 'general',
      userId: userId || 'anonymous'
    };

    return this.http.post<ChatMessage>(`${this.apiUrl}/chat`, request);
  }

  checkHealth(): Observable<LLMHealth> {
    return this.http.get<LLMHealth>(`${this.apiUrl}/health`);
  }

  getEmergencyGuidance(type: string, userId?: string): Observable<ChatMessage> {
    return this.http.get<ChatMessage>(
      `${this.apiUrl}/emergency-guidance`,
      {
        params: {
          type: type,
          ...(userId && { userId: userId })
        }
      }
    );
  }

  analyzeIncident(description: string, userId?: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(
      `${this.apiUrl}/analyze-incident?description=${encodeURIComponent(description)}`,
      {},
      {
        params: userId ? { userId: userId } : {}
      }
    );
  }

  getIncidentGuidance(message: string, userId?: string): Observable<ChatMessage> {
    const request = {
      message: message,
      context: 'incident',
      userId: userId || 'anonymous'
    };

    return this.http.post<ChatMessage>(`${this.apiUrl}/incident-guidance`, request);
  }

  getAlertGuidance(message: string, userId?: string): Observable<ChatMessage> {
    const request = {
      message: message,
      context: 'alert',
      userId: userId || 'anonymous'
    };

    return this.http.post<ChatMessage>(`${this.apiUrl}/alert-guidance`, request);
  }
}
