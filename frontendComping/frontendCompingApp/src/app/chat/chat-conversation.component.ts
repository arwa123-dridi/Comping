import { Component, OnDestroy, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
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

  newMessage = '';
  loading = false;
  sending = false;

  // Voice recording
  recording = false;
  sendingVoice = false;
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
    this.conversationId = this.route.snapshot.paramMap.get('id') ?? '';
    this.currentUserKey = this.community.getCurrentEmail();

    this.loadConversation();
    this.loadMessages();
    this.connectRealtime();

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
    this.community.getMessages(this.conversationId).subscribe({
      next: messages => {
        this.messages = messages;
        this.loading = false;
        this.shouldScroll = true;
        this.community.markAsRead(this.conversationId).subscribe({ error: () => {} });
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
      this.community.sendVoiceMessage(this.conversationId, blob).subscribe({
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
  sendVoiceFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.conversationId) return;

    this.sendingVoice = true;
    this.community.sendVoiceMessage(this.conversationId, file).subscribe({
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
    return 'http://localhost:8087' + message.contenu;
  }

  // ===================================================
  // REAL-TIME SOCKET
  // ===================================================

  private connectRealtime(): void {
    if (!this.conversationId || this.socket) return;
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

    ws.onmessage = (e) => this.handleStompFrame(String(e.data));
    ws.onclose = () => {
      this.realtimeConnected = false;
      this.socket = undefined;
      this.reconnectHandle = window.setTimeout(() => this.connectRealtime(), 5000);
    };
    ws.onerror = () => this.realtimeConnected = false;
  }

  private disconnectRealtime(): void {
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
      const command = frame.split('\n', 1)[0];
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
        const body = frame.substring(frame.indexOf('\n\n') + 2);
        this.handleRealtimeBody(body);
      }
    });
  }

  private handleRealtimeBody(body: string): void {
    try {
      const payload = JSON.parse(body);
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
