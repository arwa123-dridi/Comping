import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject, map } from 'rxjs';

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

export interface ReviewTargetOption {
  id: string;
  label: string;
  details?: string;
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
  likedByCurrentUser?: boolean;
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

export interface AbonnementResponse {
  id: string;
  suiviId: string;
  suiviNom: string;
  suiviEmail: string;
}

export interface CampeurInfo {
  id: string;   // email — identifiant universel cohérent avec auteurId des posts
  nom: string;
  photo?: string;
}

export interface UserStatus {
  userId: string;
  nom: string;
  online: boolean;
  lastSeen?: string;
  statusMessage?: string;
}

export interface SocialNotification {
  type: 'NEW_MESSAGE' | 'USER_STATUS' | 'INCOMING_CALL' | 'NEW_GROUP' | 'REACTION' | 'COMMENT' | 'REPLY' | 'NEW_POST' | 'NEW_FOLLOWER' | 'MENTION' | 'NEW_AVIS' | 'AVIS_VALIDE' | 'AVIS_REJETE';
  conversationId?: string;
  expediteurNom?: string;
  userId?: string;
  online?: boolean;
  callType?: 'AUDIO' | 'VIDEO';
  from?: string;
  groupId?: string;
  nom?: string;
  postId?: string;
  commentId?: string;
  emoji?: string;
}

export interface AppNotification {
  id: string;
  type: SocialNotification['type'];
  message: string;
  read: boolean;
  timestamp: Date;
  postId?: string;
  avisId?: string;
  emoji?: string;
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

  // === Notifications applicatives (cloche) ===
  private _appNotifs = new BehaviorSubject<AppNotification[]>([]);
  public appNotifications$ = this._appNotifs.asObservable();
  public unreadCount$ = this._appNotifs.pipe(map(n => n.filter(x => !x.read).length));

  private socket?: WebSocket;
  private reconnectTimer?: number;
  private subId = 0;

  constructor(private http: HttpClient) {}

  // === AUTH HELPERS ===
  getRole(): string { return this.decodeToken()?.role ?? 'CLIENT'; }
  isAdmin(): boolean { return this.getRole().toUpperCase() === 'ADMIN'; }
  isOrganisateur(): boolean { return this.getRole().toUpperCase() === 'ORGANISATEUR'; }
  hasSocialAccess(): boolean {
    const r = this.getRole().toUpperCase();
    return ['ADMIN', 'USER', 'ORGANISATEUR', 'PROPRIETAIRE_SITE', 'BOUTIQUE', 'PARTENAIRE_LOGISTIQUE'].includes(r);
  }
  getCurrentEmail(): string { return this.decodeToken()?.sub ?? ''; }
  getCurrentUserId(): string { return this.decodeToken()?.id ?? this.getCurrentEmail(); }

  // === GLOBAL NOTIFICATIONS SOCKET ===
  connectNotificationsSocket(): void {
    // Ne pas reconnecter si déjà ouvert ou en cours de connexion
    if (this.socket &&
        (this.socket.readyState === WebSocket.OPEN ||
         this.socket.readyState === WebSocket.CONNECTING)) return;

    const token = localStorage.getItem('authToken');
    if (!token) return;

    // /ws-chat = endpoint natif (sans SockJS). /ws-chat/websocket est le chemin interne SockJS.
    const ws = new WebSocket('ws://localhost:8087/ws-chat');
    this.socket = ws;

    ws.onopen = () => {
      this.sendFrame('CONNECT', {
        'accept-version': '1.2,1.1,1.0',
        'heart-beat': '0,0',          // désactivé pour éviter les déconnexions parasites
        'Authorization': `Bearer ${token}`
      });
    };

    ws.onmessage = e => this.handleStomp(String(e.data));

    ws.onclose = () => {
      this.socket = undefined;
      // Reconnexion automatique après 3 s si le token est toujours présent
      if (localStorage.getItem('authToken')) {
        this.reconnectTimer = window.setTimeout(() => this.connectNotificationsSocket(), 3000);
      }
    };

    ws.onerror = () => {
      // onerror est toujours suivi de onclose → la reconnexion se fait là
    };
  }

  disconnectNotificationsSocket(): void {
    if (this.reconnectTimer) window.clearTimeout(this.reconnectTimer);
    if (this.socket) {
      if (this.socket.readyState === WebSocket.OPEN) {
        this.sendFrame('DISCONNECT', {});
      }
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
    // Un message WebSocket peut contenir plusieurs frames STOMP (rare mais possible)
    const frames = raw.split('\0').filter(f => f.trim().length > 0);
    for (const frame of frames) {
      // trim() gère les éventuels \r en fin de ligne (Windows line endings)
      const command = frame.split('\n')[0].trim();

      if (command === 'CONNECTED') {
        const email = this.getCurrentEmail();
        if (email) {
          this.sendFrame('SUBSCRIBE', {
            id: `sub-notif-${++this.subId}`,
            destination: `/topic/user/${email}/notifications`
          });
        }
      } else if (command === 'MESSAGE') {
        const bodyStart = frame.indexOf('\n\n');
        if (bodyStart === -1) continue;
        const body = frame.substring(bodyStart + 2).trim();
        if (!body) continue;
        try {
          const payload = JSON.parse(body) as SocialNotification;
          this.notifications$.next(payload);
          this.pushAppNotification(payload);
          if (payload.type === 'USER_STATUS' && payload.userId !== undefined) {
            this.userStatusChanges$.next({ userId: payload.userId, online: !!payload.online });
            const current = new Set(this.onlineUsers$.value);
            if (payload.online) current.add(payload.userId);
            else current.delete(payload.userId);
            this.onlineUsers$.next(current);
          }
        } catch { /* frame malformée — ignorée */ }
      }
      // HEARTBEAT (\n seul) et ERROR sont ignorés silencieusement
    }
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

  updateAvis(id: string, payload: AvisRequest): Observable<AvisResponse> {
    return this.http.put<AvisResponse>(`${this.baseUrl}/api/avis/${id}`, payload, { headers: this.authHeaders() });
  }

  deleteMyAvis(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/avis/${id}`, { headers: this.authHeaders() });
  }

  getAllValidatedAvis(): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(`${this.baseUrl}/api/avis/valides`, { headers: this.authHeaders() });
  }

  getFriendsAvis(): Observable<AvisResponse[]> {
    return this.http.get<AvisResponse[]>(`${this.baseUrl}/api/avis/amis`, { headers: this.authHeaders() });
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

  getReviewTargets(typeCible: TargetType): Observable<ReviewTargetOption[]> {
    const headers = this.authHeaders();
    switch (typeCible) {
      case 'EVENEMENT':
        return this.http.get<any[]>(`${this.baseUrl}/api/events`, { headers }).pipe(
          map(items => items.map(item => ({
            id: item.idEvent,
            label: item.titre || item.nom || item.idEvent,
            details: item.lieu || item.categorie
          })).filter(item => !!item.id))
        );
      case 'ACTIVITE':
        return this.http.get<any[]>(`${this.baseUrl}/api/activities/GetAllActivities`, { headers }).pipe(
          map(items => items.map(item => ({
            id: item.idActivity,
            label: item.nom || item.type || item.idActivity,
            details: item.type || item.duree
          })).filter(item => !!item.id))
        );
      case 'SITE_CAMPING':
        return this.http.get<any[]>(`${this.baseUrl}/api/sites`, { headers }).pipe(
          map(items => items.map(item => ({
            id: item.id,
            label: item.nom || item.localisation || item.id,
            details: item.localisation
          })).filter(item => !!item.id))
        );
      case 'PRODUIT':
        return this.http.get<any[]>(`${this.baseUrl}/api/produits/allProduct`, { headers }).pipe(
          map(items => items.map(item => ({
            id: item.id,
            label: item.nomProduit || item.descriptionProduit || item.id,
            details: item.categorieProduit
          })).filter(item => !!item.id))
        );
    }
  }

  // ============================================================
  // POSTS
  // ============================================================
  // ============================================================
  // ABONNEMENTS (follow/unfollow)
  // ============================================================
  getCampeurs(): Observable<CampeurInfo[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/users`, { headers: this.authHeaders() }).pipe(
      map(users => users
        .filter(u => !!u.email)
        .map(u => ({
          id: u.email as string,
          nom: (`${u.firstName ?? ''} ${u.lastName ?? ''}`).trim() || (u.email as string) || 'Campeur',
          photo: u.photo as string | undefined
        }))
      )
    );
  }

  followUser(suiviId: string): Observable<AbonnementResponse> {
    return this.http.post<AbonnementResponse>(
      `${this.baseUrl}/api/abonnements/suivre`, { suiviId }, { headers: this.authHeaders() });
  }

  unfollowUser(suiviId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/abonnements/retirer/${encodeURIComponent(suiviId)}`, { headers: this.authHeaders() });
  }

  getMyFollowing(): Observable<AbonnementResponse[]> {
    return this.http.get<AbonnementResponse[]>(
      `${this.baseUrl}/api/abonnements/mes-abonnements`, { headers: this.authHeaders() });
  }

  getFriendsPosts(page = 0, size = 20): Observable<PostResponse[]> {
    return this.http.get<PostResponse[]>(
      `${this.baseUrl}/api/posts/amis?page=${page}&size=${size}`, { headers: this.authHeaders() });
  }

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

  createPostWithImages(payload: PostRequest, images: File[]): Observable<PostResponse> {
    const formData = new FormData();
    formData.append('post', JSON.stringify(payload));
    images.forEach(image => formData.append('images', image));
    return this.http.post<PostResponse>(`${this.baseUrl}/api/posts`, formData, { headers: this.authHeaders() });
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

  createComment(postId: string, contenu: string, parentCommentId?: string, mentionedIds?: string[]): Observable<CommentaireResponse> {
    return this.http.post<CommentaireResponse>(
      `${this.baseUrl}/api/posts/${postId}/comments`,
      { postId, contenu, parentCommentId, mentionedIds },
      { headers: this.authHeaders() });
  }

  updateComment(postId: string, commentId: string, contenu: string): Observable<CommentaireResponse> {
    return this.http.put<CommentaireResponse>(
      `${this.baseUrl}/api/posts/${postId}/comments/${commentId}`,
      { contenu },
      { headers: this.authHeaders() });
  }

  deleteComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/posts/${postId}/comments/${commentId}`,
      { headers: this.authHeaders() });
  }

  likeComment(postId: string, commentId: string): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/api/posts/${postId}/comments/${commentId}/like`,
      {}, { headers: this.authHeaders() });
  }

  unlikeComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/posts/${postId}/comments/${commentId}/like`,
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

  updateMessage(messageId: string, contenu: string): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(
      `${this.baseUrl}/api/chat/message/${encodeURIComponent(messageId)}`,
      { contenu },
      { headers: this.authHeaders() });
  }

  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/chat/message/${encodeURIComponent(messageId)}`,
      { headers: this.authHeaders() });
  }

  deleteConversation(conversationId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/chat/conversation/${encodeURIComponent(conversationId)}`,
      { headers: this.authHeaders() });
  }

  sendVoiceMessage(conversationId: string, audio: Blob, fileName = 'message.wav'): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('audio', audio, fileName);
    formData.append('conversationId', conversationId);
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/api/chat/voice`, formData, { headers: this.authHeaders() });
  }

  sendAttachment(conversationId: string, file: File): Observable<MessageResponse> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    formData.append('conversationId', conversationId);
    return this.http.post<MessageResponse>(
      `${this.baseUrl}/api/chat/attachment`, formData, { headers: this.authHeaders() });
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

  // === NOTIFICATIONS APPLICATIVES ===
  markAllNotificationsRead(): void {
    this._appNotifs.next(this._appNotifs.value.map(n => ({ ...n, read: true })));
  }

  clearNotifications(): void {
    this._appNotifs.next([]);
  }

  private pushAppNotification(raw: SocialNotification): void {
    if (raw.type === 'USER_STATUS') return; // statut en ligne = pas de cloche
    const labels: Partial<Record<SocialNotification['type'], string>> = {
      COMMENT:      `💬 ${raw.expediteurNom || 'Quelqu\'un'} a commenté votre publication`,
      REPLY:        `↩️ ${raw.expediteurNom || 'Quelqu\'un'} a répondu à votre commentaire`,
      NEW_POST:     `📢 ${raw.expediteurNom || 'Un ami'} a publié une nouvelle publication`,
      NEW_MESSAGE:  `✉️ ${raw.expediteurNom || 'Quelqu\'un'} vous a envoyé un message`,
      NEW_GROUP:    `👥 Vous avez été ajouté au groupe ${raw.nom || ''}`,
      INCOMING_CALL:`📞 Appel ${raw.callType === 'VIDEO' ? 'vidéo' : 'audio'} de ${raw.from || ''}`,
      REACTION:     `${raw.emoji || '👍'} ${raw.expediteurNom || 'Quelqu\'un'} a réagi à votre publication`,
      NEW_FOLLOWER: `👤 ${raw.expediteurNom || 'Quelqu\'un'} a commencé à vous suivre`,
      MENTION:     `@ ${raw.expediteurNom || 'Quelqu\'un'} vous a mentionné dans un commentaire`,
      NEW_AVIS:    `⭐ ${raw.expediteurNom || 'Quelqu\'un'} a déposé un nouvel avis`,
      AVIS_VALIDE: `✅ Votre avis a été validé par Campino`,
      AVIS_REJETE: `❌ Votre avis a été rejeté par Campino`,
    };
    const notif: AppNotification = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      type: raw.type,
      message: labels[raw.type] ?? 'Nouvelle notification',
      read: false,
      timestamp: new Date(),
      postId: raw.postId,
      avisId: (raw as any).avisId,
      emoji: raw.emoji,
    };
    this._appNotifs.next([notif, ...this._appNotifs.value].slice(0, 50));
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
