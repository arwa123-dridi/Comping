import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TargetType = 'SITE_CAMPING' | 'PRODUIT' | 'EVENEMENT' | 'ACTIVITE';
export type AvisStatus = 'EN_ATTENTE' | 'VALIDE' | 'REJETE';

export interface AvisRequest {
  note: number;
  commentaire: string;
  cibleId: string;
  typeCible: TargetType;
  parentAvisId?: string;
}

export interface AvisResponse {
  id: string;
  note: number;
  commentaire: string;
  datePublication: string;
  statut: AvisStatus;
  valide: boolean;
  utilisateurId: string;
  utilisateurNom: string;
  cibleId: string;
  typeCible: TargetType;
  parentAvisId?: string;
  enfants?: AvisResponse[];
}

export interface AvisStats {
  nombreTotal: number;
  noteMoyenne: number;
  nombre5Etoiles: number;
  nombre4Etoiles: number;
  nombre3Etoiles: number;
  nombre2Etoiles: number;
  nombre1Etoile: number;
}

export interface PostRequest {
  avisId?: string;
  cibleType?: string;
  cibleId?: string;
  contenu: string;
  images: string[];
}

export interface PostResponse {
  id: string;
  auteurNom: string;
  typePost: string;
  avisId?: string;
  cibleType?: string;
  cibleId?: string;
  contenu: string;
  images: string[];
  datePublication: string;
  likesCount: number;
  commentairesCount: number;
}

export interface CommentaireResponse {
  id: string;
  postId: string;
  parentCommentId?: string;
  auteurId: string;
  auteurNom: string;
  contenu: string;
  datePublication: string;
  likesCount: number;
  niveau: number;
  replies?: CommentaireResponse[];
}

export interface ConversationResponse {
  id: string;
  participant1Id: string;
  participant1Nom: string;
  participant2Id: string;
  participant2Nom: string;
  avisId?: string;
  messagesNonLus: number;
  dateDernierMessage: string;
}

export interface MessageResponse {
  id: string;
  conversationId: string;
  expediteurNom: string;
  contenu: string;
  typeMessage: string;
  lu: boolean;
  dateCreation: string;
  transcription?: string;
}

interface JwtPayload {
  sub?: string;
  id?: string;
  role?: string;
}

@Injectable({ providedIn: 'root' })
export class CommunityService {
  private readonly baseUrl = 'http://localhost:8087';

  constructor(private http: HttpClient) {}

  getRole(): string {
    return this.decodeToken()?.role ?? 'CLIENT';
  }

  isAdmin(): boolean {
    return this.getRole().toUpperCase() === 'ADMIN';
  }

  getCurrentEmail(): string {
    return this.decodeToken()?.sub ?? '';
  }

  getCurrentUserId(): string {
    return this.decodeToken()?.id ?? this.getCurrentEmail();
  }

  getAvisByTarget(cibleId: string, typeCible: TargetType): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(
      `${this.baseUrl}/api/avis/cible/${encodeURIComponent(cibleId)}?typeCible=${typeCible}`,
      { headers: this.authHeaders() }
    );
  }

  getMyAvis(): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(`${this.baseUrl}/api/avis/mes-avis`, { headers: this.authHeaders() });
  }

  getAvisByStatus(statut: AvisStatus): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(`${this.baseUrl}/api/avis/statut/${statut}`, { headers: this.authHeaders() });
  }

  getAvisById(id: string): Observable<AvisResponse> {
    return this.http.get<AvisResponse>(`${this.baseUrl}/api/avis/${id}`, { headers: this.authHeaders() });
  }

  getAvisStats(cibleId: string, typeCible: TargetType): Observable<AvisStats> {
    return this.http.get<AvisStats>(
      `${this.baseUrl}/api/avis/statistiques/${encodeURIComponent(cibleId)}?typeCible=${typeCible}`,
      { headers: this.authHeaders() }
    );
  }

  createAvis(payload: AvisRequest): Observable<AvisResponse> {
    return this.http.post<AvisResponse>(`${this.baseUrl}/api/avis`, payload, { headers: this.authHeaders() });
  }

  validateAvis(id: string): Observable<AvisResponse> {
    return this.http.post<AvisResponse>(`${this.baseUrl}/api/avis/${id}/valider`, {}, { headers: this.authHeaders() });
  }

  rejectAvis(id: string, motif: string): Observable<AvisResponse> {
    return this.http.post<AvisResponse>(
      `${this.baseUrl}/api/avis/${id}/rejeter?motif=${encodeURIComponent(motif)}`,
      {},
      { headers: this.authHeaders() }
    );
  }

  getFeed(page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(`${this.baseUrl}/api/posts/feed?page=${page}&size=${size}`, { headers: this.authHeaders() });
  }

  getPost(id: string): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.baseUrl}/api/posts/${id}`, { headers: this.authHeaders() });
  }

  createPost(payload: PostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/api/posts`, payload, { headers: this.authHeaders() });
  }

  likePost(id: string): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/api/posts/${id}/like`, {}, { headers: this.authHeaders() });
  }

  unlikePost(id: string): Observable<PostResponse> {
    return this.http.delete<PostResponse>(`${this.baseUrl}/api/posts/${id}/like`, { headers: this.authHeaders() });
  }

  getComments(postId: string): Observable<CommentaireResponse[]> {
    return this.http.get<CommentaireResponse[]>(`${this.baseUrl}/api/posts/${postId}/comments`, { headers: this.authHeaders() });
  }

  createComment(postId: string, contenu: string, parentCommentId?: string): Observable<CommentaireResponse> {
    const payload = { postId, contenu, parentCommentId };
    return this.http.post<CommentaireResponse>(`${this.baseUrl}/api/posts/${postId}/comments`, payload, { headers: this.authHeaders() });
  }

  getConversations(): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(`${this.baseUrl}/api/chat/conversations`, { headers: this.authHeaders() });
  }

  getOrCreateConversation(participant2Id: string, avisId?: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(
      `${this.baseUrl}/api/chat/conversation`,
      { participant2Id, avisId },
      { headers: this.authHeaders() }
    );
  }

  getMessages(conversationId: string, page = 0, size = 50): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(
      `${this.baseUrl}/api/chat/messages/${conversationId}?page=${page}&size=${size}`,
      { headers: this.authHeaders() }
    );
  }

  sendMessage(conversationId: string, contenu: string, typeMessage = 'TEXT'): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/api/chat/message`,
      { conversationId, contenu, typeMessage },
      { headers: this.authHeaders() }
    );
  }

  markAsRead(conversationId: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/api/chat/messages/${conversationId}/read`, {}, { headers: this.authHeaders() });
  }

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }

  private decodeToken(): JwtPayload | null {
    const token = localStorage.getItem('authToken');
    if (!token) {
      return null;
    }
    try {
      const payload = token.split('.')[1];
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(normalized)) as JwtPayload;
    } catch {
      return null;
    }
  }
}
