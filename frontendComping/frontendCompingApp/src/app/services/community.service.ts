import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

// ============================================================
// MODELS
// ============================================================
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
  visibilite?: 'PUBLIC' | 'AMIS' | 'PRIVE';
}

export interface PostResponse {
  id: string;
  auteurId: string;
  auteurNom: string;
  auteurPhoto?: string;
  typePost: string;
  avisId?: string;
  cibleType?: string;
  cibleId?: string;
  contenu: string;
  images: string[];
  datePublication: string;
  likesCount: number;
  commentairesCount: number;
  likedByCurrentUser: boolean;
  reactions?: Record<string, number>;
  myReaction?: string;
  hashtags?: string[];
  trendScore?: number;
  visibilite?: string;
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
  participant1Id?: string;
  participant1Nom?: string;
  participant2Id?: string;
  participant2Nom?: string;
  groupe?: boolean;
  nomGroupe?: string;
  avatarGroupe?: string;
  participantIds?: string[];
  participantNoms?: string[];
  avisId?: string;
  messagesNonLus: number;
  dateDernierMessage: string;
  dernierMessageContenu?: string;
  autreParticipantEnLigne?: boolean;
}

export interface MessageResponse {
  id: string;
  conversationId: string;
  expediteurId: string;
  destinataireId: string;
  expediteurNom: string;
  contenu: string;
  typeMessage: string;
  lu: boolean;
  dateCreation: string;
  transcription?: string;
  callData?: string;
}

export interface UserStatus {
  userId: string;
  nom: string;
  online: boolean;
  lastSeen?: string;
  statusMessage?: string;
}

export interface SocialNotification {
  type: 'NEW_MESSAGE' | 'USER_STATUS' | 'INCOMING_CALL' | 'NEW_GROUP' | 'REACTION' | 'COMMENT';
  conversationId?: string;
  expediteurNom?: string;
  userId?: string;
  online?: boolean;
  callType?: 'AUDIO' | 'VIDEO';
  from?: string;
  groupId?: string;
  nom?: string;
}

interface JwtPayload { sub?: string; id?: string; role?: string; }

// ============================================================
// SERVICE
// ============================================================
@Injectable({ providedIn: 'root' })
export class CommunityService {
  private readonly baseUrl = 'http://localhost:8087';

  // === Real-time WebSocket subjects ===
  public notifications$ = new Subject<SocialNotification>();
  public userStatusChanges$ = new Subject<{ userId: string; online: boolean }>();
  public onlineUsers$ = new BehaviorSubject<Set<string>>(new Set<string>());

  private socket?: WebSocket;
  private reconnectTimer?: number;
  private subId = 0;

  constructor(private http: HttpClient) {}

  // === AUTH HELPERS ===
  getRole(): string { return this.decodeToken()?.role ?? 'CLIENT'; }
  isAdmin(): boolean { return this.getRole().toUpperCase() === 'ADMIN'; }
  getCurrentEmail(): string { return this.decodeToken()?.sub ?? ''; }
  getCurrentUserId(): string { return this.decodeToken()?.id ?? this.getCurrentEmail(); }

  // === GLOBAL NOTIFICATIONS SOCKET ===
  connectNotificationsSocket(): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) return;
    const token = localStorage.getItem('authToken');
    if (!token) return;

    const ws = new WebSocket('ws://localhost:8087/ws-chat/websocket');
    this.socket = ws;

    ws.onopen = () => {
      this.sendFrame('CONNECT', {
        'accept-version': '1.2',
        'heart-beat': '10000,10000',
        'Authorization': `Bearer ${token}`
      });
    };

    ws.onmessage = e => this.handleStomp(String(e.data));
    ws.onclose = () => {
      this.socket = undefined;
      this.reconnectTimer = window.setTimeout(() => this.connectNotificationsSocket(), 5000);
    };
    ws.onerror = () => {};
  }

  disconnectNotificationsSocket(): void {
    if (this.reconnectTimer) window.clearTimeout(this.reconnectTimer);
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.sendFrame('DISCONNECT', {});
      this.socket.close();
    }
    this.socket = undefined;
  }

  private sendFrame(command: string, headers: Record<string, string>, body = ''): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`).join('\n');
    this.socket.send(`${command}\n${headerLines}\n\n${body}\0`);
  }

  private handleStomp(raw: string): void {
    const frames = raw.split('\0').filter(f => f.trim().length > 0);
    frames.forEach(frame => {
      const command = frame.split('\n', 1)[0];
      if (command === 'CONNECTED') {
        const email = this.getCurrentEmail();
        if (email) {
          this.sendFrame('SUBSCRIBE', {
            id: `sub-notif-${++this.subId}`,
            destination: `/topic/user/${email}/notifications`
          });
        }
      } else if (command === 'MESSAGE') {
        const body = frame.substring(frame.indexOf('\n\n') + 2);
        try {
          const payload = JSON.parse(body) as SocialNotification;
          this.notifications$.next(payload);
          if (payload.type === 'USER_STATUS' && payload.userId !== undefined) {
            this.userStatusChanges$.next({ userId: payload.userId, online: !!payload.online });
            const current = new Set(this.onlineUsers$.value);
            if (payload.online) current.add(payload.userId);
            else current.delete(payload.userId);
            this.onlineUsers$.next(current);
          }
        } catch {}
      }
    });
  }

  // ============================================================
  // AVIS
  // ============================================================
  getAvisByTarget(cibleId: string, typeCible: TargetType): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(
      `${this.baseUrl}/api/avis/cible/${encodeURIComponent(cibleId)}?typeCible=${typeCible}`,
      { headers: this.authHeaders() });
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
      { headers: this.authHeaders() });
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
      {}, { headers: this.authHeaders() });
  }

  // ============================================================
  // POSTS
  // ============================================================
  getFeed(page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(
      `${this.baseUrl}/api/posts/feed?page=${page}&size=${size}`, { headers: this.authHeaders() });
  }

  getTrending(page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(
      `${this.baseUrl}/api/posts/trending?page=${page}&size=${size}`, { headers: this.authHeaders() });
  }

  getByHashtag(hashtag: string, page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(
      `${this.baseUrl}/api/posts/hashtag/${encodeURIComponent(hashtag)}?page=${page}&size=${size}`,
      { headers: this.authHeaders() });
  }

  getPost(id: string): Observable<PostResponse> {
    return this.http.get<PostResponse>(`${this.baseUrl}/api/posts/${id}`, { headers: this.authHeaders() });
  }

  createPost(payload: PostRequest): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/api/posts`, payload, { headers: this.authHeaders() });
  }

  deletePost(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/posts/${id}`, { headers: this.authHeaders() });
  }

  updatePost(id: string, payload: PostRequest): Observable<PostResponse> {
    return this.http.put<PostResponse>(`${this.baseUrl}/api/posts/${id}`, payload, { headers: this.authHeaders() });
  }

  getUserPosts(userId: string, page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(
      `${this.baseUrl}/api/posts/user/${encodeURIComponent(userId)}?page=${page}&size=${size}`,
      { headers: this.authHeaders() });
  }

  likePost(id: string): Observable<PostResponse> {
    return this.http.post<PostResponse>(`${this.baseUrl}/api/posts/${id}/like`, {}, { headers: this.authHeaders() });
  }

  unlikePost(id: string): Observable<PostResponse> {
    return this.http.delete<PostResponse>(`${this.baseUrl}/api/posts/${id}/like`, { headers: this.authHeaders() });
  }

  reactToPost(id: string, emoji: string): Observable<PostResponse> {
    return this.http.post<PostResponse>(
      `${this.baseUrl}/api/posts/${id}/react`, { emoji }, { headers: this.authHeaders() });
  }

  removeReaction(id: string): Observable<PostResponse> {
    return this.http.delete<PostResponse>(`${this.baseUrl}/api/posts/${id}/react`, { headers: this.authHeaders() });
  }

  getComments(postId: string): Observable<CommentaireResponse[]> {
    return this.http.get<CommentaireResponse[]>(
      `${this.baseUrl}/api/posts/${postId}/comments`, { headers: this.authHeaders() });
  }

  createComment(postId: string, contenu: string, parentCommentId?: string): Observable<CommentaireResponse> {
    return this.http.post<CommentaireResponse>(
      `${this.baseUrl}/api/posts/${postId}/comments`,
      { postId, contenu, parentCommentId },
      { headers: this.authHeaders() });
  }

  // ============================================================
  // CHAT
  // ============================================================
  getConversations(): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(
      `${this.baseUrl}/api/chat/conversations`, { headers: this.authHeaders() });
  }

  getOrCreateConversation(participant2Id: string, avisId?: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(
      `${this.baseUrl}/api/chat/conversation`,
      { participant2Id, avisId },
      { headers: this.authHeaders() });
  }

  createGroup(nomGroupe: string, participantIds: string[], avatarGroupe?: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(
      `${this.baseUrl}/api/chat/group`,
      { nomGroupe, participantIds, avatarGroupe },
      { headers: this.authHeaders() });
  }

  addToGroup(conversationId: string, participantId: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(
      `${this.baseUrl}/api/chat/group/${conversationId}/add?participantId=${encodeURIComponent(participantId)}`,
      {}, { headers: this.authHeaders() });
  }

  removeFromGroup(conversationId: string, participantId: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(
      `${this.baseUrl}/api/chat/group/${conversationId}/remove?participantId=${encodeURIComponent(participantId)}`,
      {}, { headers: this.authHeaders() });
  }

  getMessages(conversationId: string, page = 0, size = 50): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(
      `${this.baseUrl}/api/chat/messages/${conversationId}?page=${page}&size=${size}`,
      { headers: this.authHeaders() });
  }

  sendMessage(conversationId: string, contenu: string, typeMessage = 'TEXT'): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/api/chat/message`,
      { conversationId, contenu, typeMessage },
      { headers: this.authHeaders() });
  }

  sendVoiceMessage(conversationId: string, audio: Blob): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('audio', audio, 'message.wav');
    formData.append('conversationId', conversationId);
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/api/chat/voice`, formData, { headers: this.authHeaders() });
  }

  sendCallSignal(conversationId: string, signalData: string, callType: 'AUDIO' | 'VIDEO' = 'VIDEO'): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/api/chat/call/${conversationId}/signal?callType=${callType}`,
      signalData,
      { headers: this.authHeaders().set('Content-Type', 'text/plain') });
  }

  getUserStatus(userId: string): Observable<UserStatus> {
    return this.http.get<UserStatus>(
      `${this.baseUrl}/api/chat/status/${encodeURIComponent(userId)}`,
      { headers: this.authHeaders() });
  }

  getOnlineUsers(): Observable<UserStatus[]> {
    return this.http.get<UserStatus[]>(`${this.baseUrl}/api/chat/online`, { headers: this.authHeaders() });
  }

  markAsRead(conversationId: string): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/api/chat/messages/${conversationId}/read`, {},
      { headers: this.authHeaders() });
  }

  // === HELPERS ===
  authHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }

  private decodeToken(): JwtPayload | null {
    const token = localStorage.getItem('authToken');
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(normalized)) as JwtPayload;
    } catch { return null; }
  }
}
