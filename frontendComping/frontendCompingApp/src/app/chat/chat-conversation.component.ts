import { Component, OnDestroy, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CommunityService, MessageResponse, ConversationResponse
} from '../services/community.service';
import { VoiceRecorderService } from '../services/voice-recorder.service';
import { WebRtcService } from '../services/webrtc.service';

@Component({
  selector: 'app-chat-conversation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-conversation.component.html',
  styleUrls: ['./chat-conversation.component.css']
})
export class ChatConversationComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesPanel') messagesPanel?: ElementRef<HTMLElement>;

  conversationId = '';
  conversation: ConversationResponse | null = null;
  messages: MessageResponse[] = [];
  selectedMessageIds = new Set<string>();
  editingMessageId: string | null = null;
  editMessageText = '';

  newMessage = '';
  loading = false;
  sending = false;

  // Voice recording
  recording = false;
  sendingVoice = false;
  sendingAttachment = false;
  recordingDuration = 0;
  private recordingTimer?: number;

  // Calls
  callActive = false;
  callType: 'AUDIO' | 'VIDEO' | null = null;
  incomingCall = false;
  callerName = '';

  // Status
  realtimeConnected = false;
  otherOnline = false;
  otherTyping = false; // Future enhancement

  error = '';
  success = '';
  currentUserKey = '';

  private subs: Subscription[] = [];
  private socket?: WebSocket;
  private subId = 0;
  private reconnectHandle?: number;
  private shouldScroll = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public community: CommunityService,
    private voiceRecorder: VoiceRecorderService,
    public webrtc: WebRtcService
  ) {}

  ngOnInit(): void {
    this.currentUserKey = this.community.getCurrentEmail();

    // S'abonner à paramMap (observable) pour réagir aux changements de conversation
    // sans rechargement de page (Angular réutilise le composant sur /messages/:id)
    this.subs.push(
      this.route.paramMap.subscribe(params => {
        const newId = params.get('id') ?? '';
        if (newId === this.conversationId) return;
        this.switchConversation(newId);
      })
    );

    // Listen to user status changes
    this.subs.push(
      this.community.userStatusChanges$.subscribe(({ userId, online }) => {
        if (this.conversation && !this.conversation.groupe) {
          const otherId = this.getOtherParticipantId();
          if (otherId === userId) {
            this.otherOnline = online;
            this.success = online ? `🟢 ${this.getOtherName()} vient de se connecter` : `${this.getOtherName()} s'est déconnecté`;
            setTimeout(() => this.success = '', 4000);
          }
        }
      })
    );

    // Listen to incoming calls
    this.subs.push(
      this.webrtc.callStateChange.subscribe(state => {
        this.callActive = state.active;
        this.callType = state.callType;
        this.incomingCall = state.incoming || false;
        this.callerName = state.remoteUserName || '';
      })
    );
  }

  private switchConversation(id: string): void {
    // Couper le socket de l'ancienne conversation avant d'en ouvrir une nouvelle
    this.disconnectRealtime();

    // Réinitialiser l'état
    this.conversationId = id;
    this.messages = [];
    this.conversation = null;
    this.selectedMessageIds.clear();
    this.editingMessageId = null;
    this.editMessageText = '';
    this.newMessage = '';
    this.error = '';
    this.success = '';
    this.otherOnline = false;

    if (!id) return;

    this.loadConversation();
    this.loadMessages();
    this.connectRealtime();
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    if (this.recordingTimer) window.clearInterval(this.recordingTimer);
    if (this.reconnectHandle) window.clearTimeout(this.reconnectHandle);
    this.disconnectRealtime();
    if (this.voiceRecorder.isRecording()) this.voiceRecorder.cancelRecording();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  // ===================================================
  // LOAD DATA
  // ===================================================

  loadConversation(): void {
    this.community.getConversations().subscribe({
      next: convs => {
        this.conversation = convs.find(c => c.id === this.conversationId) || null;
        if (this.conversation) {
          this.otherOnline = !!this.conversation.autreParticipantEnLigne;
        }
      }
    });
  }

  loadMessages(showLoader = true): void {
    if (!this.conversationId) { this.error = 'Conversation introuvable.'; return; }
    this.loading = showLoader;
    this.fetchAllMessages(0, []);
  }

  private fetchAllMessages(page: number, accumulated: MessageResponse[]): void {
    const PAGE_SIZE = 100;
    this.community.getMessages(this.conversationId, page, PAGE_SIZE).subscribe({
      next: messages => {
        const all = [...accumulated, ...messages];
        if (messages.length === PAGE_SIZE) {
          // Il peut y avoir d'autres pages
          this.fetchAllMessages(page + 1, all);
        } else {
          this.messages = all;
          this.selectedMessageIds.clear();
          this.loading = false;
          this.shouldScroll = true;
          this.community.markAsRead(this.conversationId).subscribe({ error: () => {} });
        }
      },
      error: () => {
        this.error = 'Impossible de charger les messages.';
        this.loading = false;
      }
    });
  }

  // ===================================================
  // SEND MESSAGE
  // ===================================================

  sendMessage(): void {
    const contenu = this.newMessage.trim();
    if (!contenu || !this.conversationId) return;

    this.sending = true;
    this.community.sendMessage(this.conversationId, contenu).subscribe({
      next: message => {
        // WebSocket pushe déjà le message, on évite la duplication
        if (!this.messages.some(m => m.id === message.id)) {
          this.messages = [...this.messages, message];
        }
        this.newMessage = '';
        this.sending = false;
        this.shouldScroll = true;
      },
      error: () => {
        this.error = 'Message non envoyé.';
        this.sending = false;
      }
    });
  }

  toggleMessageSelection(message: MessageResponse, event: Event): void {
    event.stopPropagation();
    if (this.isCallMessage(message)) return;
    if (this.selectedMessageIds.has(message.id)) {
      this.selectedMessageIds.delete(message.id);
    } else {
      this.selectedMessageIds.add(message.id);
    }
  }

  clearMessageSelection(): void {
    this.selectedMessageIds.clear();
  }

  isMessageSelected(message: MessageResponse): boolean {
    return this.selectedMessageIds.has(message.id);
  }

  get selectedMessages(): MessageResponse[] {
    return this.messages.filter(m => this.selectedMessageIds.has(m.id));
  }

  get canEditSelectedMessage(): boolean {
    return this.selectedMessages.length === 1 && this.canMutateMessage(this.selectedMessages[0])
      && this.selectedMessages[0].typeMessage === 'TEXT';
  }

  get canDeleteSelectedMessages(): boolean {
    return this.selectedMessages.length > 0 && this.selectedMessages.every(m => this.canMutateMessage(m));
  }

  startEditSelected(): void {
    if (!this.canEditSelectedMessage) return;
    const message = this.selectedMessages[0];
    this.editingMessageId = message.id;
    this.editMessageText = message.contenu;
    this.clearMessageSelection();
  }

  saveMessageEdit(message: MessageResponse, event: Event): void {
    event.stopPropagation();
    const contenu = this.editMessageText.trim();
    if (!contenu) return;

    this.community.updateMessage(message.id, contenu).subscribe({
      next: updated => {
        this.messages = this.messages.map(m => m.id === updated.id ? updated : m);
        this.editingMessageId = null;
        this.editMessageText = '';
      },
      error: () => this.error = 'Modification impossible après 10 minutes.'
    });
  }

  cancelMessageEdit(event: Event): void {
    event.stopPropagation();
    this.editingMessageId = null;
    this.editMessageText = '';
  }

  deleteSelectedMessages(): void {
    const messages = this.selectedMessages;
    if (!messages.length || !this.canDeleteSelectedMessages) {
      this.error = 'Vous pouvez supprimer uniquement vos messages envoyés depuis moins de 10 minutes.';
      return;
    }
    if (!confirm(`Supprimer ${messages.length} message${messages.length > 1 ? 's' : ''} ?`)) return;

    forkJoin(messages.map(m => this.community.deleteMessage(m.id))).subscribe({
      next: () => {
        const ids = new Set(messages.map(m => m.id));
        this.messages = this.messages.filter(m => !ids.has(m.id));
        this.clearMessageSelection();
      },
      error: () => this.error = 'Suppression impossible après 10 minutes.'
    });
  }

  deleteConversation(): void {
    if (!this.conversationId) return;
    if (!confirm('Supprimer toute la conversation et tous ses messages ?')) return;
    this.community.deleteConversation(this.conversationId).subscribe({
      next: () => void this.router.navigate(['/messages']),
      error: () => this.error = 'Suppression de la conversation impossible.'
    });
  }

  // ===================================================
  // VOICE RECORDING
  // ===================================================

  async toggleRecording(): Promise<void> {
    if (this.recording) {
      await this.stopRecording();
    } else {
      await this.startRecording();
    }
  }

  async startRecording(): Promise<void> {
    try {
      await this.voiceRecorder.startRecording();
      this.recording = true;
      this.recordingDuration = 0;
      this.recordingTimer = window.setInterval(() => this.recordingDuration++, 1000);
    } catch (err: any) {
      this.error = err.message || 'Microphone inaccessible.';
    }
  }

  async stopRecording(): Promise<void> {
    if (this.recordingTimer) {
      window.clearInterval(this.recordingTimer);
      this.recordingTimer = undefined;
    }
    this.recording = false;
    this.sendingVoice = true;
    try {
      const blob = await this.voiceRecorder.stopRecording();
      this.community.sendVoiceMessage(this.conversationId, blob, 'message.wav').subscribe({
        next: message => {
          if (!this.messages.some(m => m.id === message.id)) {
            this.messages = [...this.messages, message];
          }
          this.sendingVoice = false;
          this.shouldScroll = true;
        },
        error: () => {
          this.error = 'Message vocal non envoyé.';
          this.sendingVoice = false;
        }
      });
    } catch (err: any) {
      this.error = err.message || 'Erreur enregistrement.';
      this.sendingVoice = false;
    }
  }

  cancelRecording(): void {
    if (this.recordingTimer) window.clearInterval(this.recordingTimer);
    this.recording = false;
    this.recordingDuration = 0;
    this.voiceRecorder.cancelRecording();
  }

  // Upload audio file from disk
  async sendVoiceFile(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.conversationId) return;

    this.sendingVoice = true;
    let audioToSend: Blob = file;
    let fileName = file.name || 'message.wav';
    try {
      audioToSend = await this.voiceRecorder.convertToWav16k(file);
      fileName = 'message.wav';
    } catch {
      this.error = 'Audio non compatible avec la transcription. Envoi du fichier original.';
    }

    this.community.sendVoiceMessage(this.conversationId, audioToSend, fileName).subscribe({
      next: message => {
        if (!this.messages.some(m => m.id === message.id)) {
          this.messages = [...this.messages, message];
        }
        this.sendingVoice = false;
        this.shouldScroll = true;
        input.value = '';
      },
      error: () => {
        this.error = 'Message vocal non envoyé. Utilisez un WAV 16kHz mono.';
        this.sendingVoice = false;
        input.value = '';
      }
    });
  }

  sendAttachment(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.conversationId) return;

    this.sendingAttachment = true;
    this.community.sendAttachment(this.conversationId, file).subscribe({
      next: message => {
        if (!this.messages.some(m => m.id === message.id)) {
          this.messages = [...this.messages, message];
        }
        this.sendingAttachment = false;
        this.shouldScroll = true;
        input.value = '';
      },
      error: () => {
        this.error = 'Pièce jointe non envoyée.';
        this.sendingAttachment = false;
        input.value = '';
      }
    });
  }

  // ===================================================
  // CALLS
  // ===================================================

  async startCall(callType: 'AUDIO' | 'VIDEO'): Promise<void> {
    if (!this.conversationId) return;
    try {
      await this.webrtc.startCall(this.conversationId, callType);
    } catch {
      this.error = 'Impossible de démarrer l\'appel. Vérifiez les permissions micro/caméra.';
    }
  }

  async acceptCall(): Promise<void> {
    try {
      await this.webrtc.acceptCall();
    } catch {
      this.error = 'Impossible d\'accepter l\'appel.';
    }
  }

  endCall(): void {
    this.webrtc.endCall();
  }

  toggleMute(): void {
    this.webrtc.toggleMute();
  }

  toggleVideo(): void {
    this.webrtc.toggleVideo();
  }

  // ===================================================
  // HELPERS
  // ===================================================

  isMine(message: MessageResponse): boolean {
    return message.expediteurId === this.currentUserKey ||
           message.expediteurNom === this.currentUserKey;
  }

  canMutateMessage(message: MessageResponse): boolean {
    if (!message || !this.isMine(message) || this.isCallMessage(message)) return false;
    const created = new Date(message.dateCreation).getTime();
    return Number.isFinite(created) && Date.now() - created <= 10 * 60 * 1000;
  }

  isCallMessage(message: MessageResponse): boolean {
    return message.typeMessage?.includes('CALL') || false;
  }

  formatCallMessage(message: MessageResponse): string {
    if (message.typeMessage?.includes('AUDIO')) return '📞 Appel audio';
    if (message.typeMessage?.includes('VIDEO')) return '📹 Appel vidéo';
    return '📞 Appel';
  }

  formatDuration(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  }

  getOtherName(): string {
    if (!this.conversation) return '';
    if (this.conversation.groupe) return this.conversation.nomGroupe || 'Groupe';
    return this.conversation.participant1Id === this.currentUserKey
      ? (this.conversation.participant2Nom || '')
      : (this.conversation.participant1Nom || '');
  }

  getOtherParticipantId(): string {
    if (!this.conversation || this.conversation.groupe) return '';
    return this.conversation.participant1Id === this.currentUserKey
      ? (this.conversation.participant2Id || '')
      : (this.conversation.participant1Id || '');
  }

  getOtherInitials(): string {
    return (this.getOtherName() || 'GR').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  getMessageInitials(message: MessageResponse): string {
    return (message.expediteurNom || 'US').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  formatTime(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  shouldShowDateSeparator(index: number): boolean {
    if (index === 0) return true;
    const prev = new Date(this.messages[index - 1].dateCreation);
    const curr = new Date(this.messages[index].dateCreation);
    return prev.toDateString() !== curr.toDateString();
  }

  formatDateSeparator(dateStr: string): string {
    const d = new Date(dateStr);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);
    if (d.toDateString() === today.toDateString()) return "Aujourd'hui";
    if (d.toDateString() === yesterday.toDateString()) return 'Hier';
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  getAudioUrl(message: MessageResponse): string {
    return this.getMediaUrl(message.contenu);
  }

  getMediaUrl(url: string): string {
    if (!url) return '';
    return url.startsWith('http') ? url : 'http://localhost:8087' + url;
  }

  isImageMessage(message: MessageResponse): boolean {
    return message.typeMessage === 'IMAGE';
  }

  isFileMessage(message: MessageResponse): boolean {
    return message.typeMessage === 'FILE';
  }

  getFileName(message: MessageResponse): string {
    const content = message.contenu || '';
    const clean = content.split('?')[0];
    return decodeURIComponent(clean.substring(clean.lastIndexOf('/') + 1)) || 'Pièce jointe';
  }

  formatTranscription(transcription?: string): string {
    if (!transcription) return '';
    try {
      const parsed = JSON.parse(transcription);
      return parsed?.text || transcription;
    } catch {
      return transcription;
    }
  }

  // ===================================================
  // REAL-TIME SOCKET
  // ===================================================

  private connectRealtime(): void {
    if (!this.conversationId || this.socket) return;
    const token = localStorage.getItem('authToken');
    if (!token) return;

    if (this.reconnectHandle) {
      window.clearTimeout(this.reconnectHandle);
      this.reconnectHandle = undefined;
    }

    const ws = new WebSocket('ws://localhost:8087/ws-chat');
    this.socket = ws;

    ws.onopen = () => {
      this.sendFrame('CONNECT', {
        'accept-version': '1.2,1.1,1.0',
        'heart-beat': '0,0',
        'Authorization': `Bearer ${token}`
      });
    };

    ws.onmessage = (e) => this.handleStompFrame(String(e.data));
    ws.onclose = () => {
      this.realtimeConnected = false;
      this.socket = undefined;
      if (this.conversationId && localStorage.getItem('authToken')) {
        this.reconnectHandle = window.setTimeout(() => this.connectRealtime(), 3000);
      }
    };
    ws.onerror = () => this.realtimeConnected = false;
  }

  private disconnectRealtime(): void {
    if (this.reconnectHandle) {
      window.clearTimeout(this.reconnectHandle);
      this.reconnectHandle = undefined;
    }
    if (!this.socket) return;
    if (this.realtimeConnected) this.sendFrame('DISCONNECT', {});
    this.socket.close();
    this.socket = undefined;
  }

  private sendFrame(command: string, headers: Record<string, string>): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    const headerLines = Object.entries(headers).map(([k, v]) => `${k}:${v}`).join('\n');
    this.socket.send(`${command}\n${headerLines}\n\n\0`);
  }

  private handleStompFrame(raw: string): void {
    const frames = raw.split('\0').filter(f => f.trim().length > 0);
    frames.forEach(frame => {
      const command = frame.split('\n')[0].trim();
      if (command === 'CONNECTED') {
        this.realtimeConnected = true;
        this.sendFrame('SUBSCRIBE', {
          id: `sub-conv-${++this.subId}`,
          destination: `/topic/conversations/${this.conversationId}`
        });
        this.sendFrame('SUBSCRIBE', {
          id: `sub-call-${++this.subId}`,
          destination: `/topic/conversations/${this.conversationId}/call`
        });
        return;
      }
      if (command === 'MESSAGE') {
        const bodyStart = frame.indexOf('\n\n');
        if (bodyStart === -1) return;
        const body = frame.substring(bodyStart + 2).trim();
        if (body) this.handleRealtimeBody(body);
      }
    });
  }

  private handleRealtimeBody(body: string): void {
    try {
      const payload = JSON.parse(body);
      if (payload.eventType === 'MESSAGE_UPDATED' && payload.message) {
        this.messages = this.messages.map(m => m.id === payload.message.id ? payload.message : m);
        return;
      }
      if (payload.eventType === 'MESSAGE_DELETED' && payload.messageId) {
        this.messages = this.messages.filter(m => m.id !== payload.messageId);
        this.selectedMessageIds.delete(payload.messageId);
        return;
      }
      if (payload.eventType === 'CONVERSATION_DELETED') {
        this.error = 'Cette conversation a été supprimée.';
        void this.router.navigate(['/messages']);
        return;
      }
      if (payload.typeMessage?.includes('CALL') || payload.callData) {
        // Geré via WebRtcService
        return;
      }
      const exists = this.messages.some(m => m.id === payload.id);
      if (!exists) {
        this.messages = [...this.messages, payload as MessageResponse];
        this.shouldScroll = true;
        this.community.markAsRead(this.conversationId).subscribe({ error: () => {} });
      }
    } catch {}
  }

  // ===================================================
  // UTILITIES
  // ===================================================

  back(): void {
    void this.router.navigate(['/messages']);
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesPanel) {
        const el = this.messagesPanel.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    } catch {}
  }
}
