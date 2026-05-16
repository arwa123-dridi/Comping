import { Injectable, EventEmitter, NgZone } from '@angular/core';
import { Subject } from 'rxjs';
import { CommunityService } from './community.service';

export interface CallState {
  active: boolean;
  callType: 'AUDIO' | 'VIDEO';
  remoteStream?: MediaStream;
  localStream?: MediaStream;
  remoteUserName?: string;
  conversationId?: string;
  incoming?: boolean;
}

export interface CallDeclinedEvent {
  callType: 'AUDIO' | 'VIDEO';
  byEmail: string;
}

@Injectable({ providedIn: 'root' })
// Gère le cycle de vie des appels audio/vidéo via WebRTC (OFFER → ANSWER → ICE candidates)
// La signalisation passe par le backend HTTP, les médias sont transmis peer-to-peer via STUN
export class WebRtcService {
  callState: CallState = { active: false, callType: 'AUDIO' };
  callStateChange = new EventEmitter<CallState>();
  // Émis quand un appel est refusé (local ou distant) — le composant chat s'y abonne pour afficher un message dans le fil
  callDeclined$ = new Subject<CallDeclinedEvent>();

  private peerConnection?: RTCPeerConnection;
  private localStream?: MediaStream;
  private conversationId = '';
  private pendingOffer?: RTCSessionDescriptionInit;

  // Guard anti-rebond : empêche la réactivation immédiate d'un appel après un refus ou une fin d'appel (fenêtre de 4s)
  private _callGuard = false;
  private _callGuardTimer?: number;

  private readonly config: RTCConfiguration = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' }
    ]
  };

  constructor(private community: CommunityService, private ngZone: NgZone) {
    this.community.notifications$.subscribe(notif => {
      if (
        notif.type === 'INCOMING_CALL' &&
        notif.conversationId &&
        notif.from !== this.community.getCurrentEmail()
      ) {
        if (this._callGuard) return;
        if (this.callState.active) return;
        this.ngZone.run(() => {
          this.callState = {
            active: true,
            callType: notif.callType || 'AUDIO',
            conversationId: notif.conversationId,
            remoteUserName: notif.from,
            incoming: true
          };
          this.callStateChange.emit(this.callState);
        });
      }
    });
  }

  async startCall(conversationId: string, callType: 'AUDIO' | 'VIDEO'): Promise<void> {
    this.conversationId = conversationId;
    this._clearGuard();

    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: callType === 'VIDEO'
      });

      this.peerConnection = new RTCPeerConnection(this.config);
      this.localStream.getTracks().forEach(t => this.peerConnection!.addTrack(t, this.localStream!));

      this.peerConnection.ontrack = (event) => {
        this.ngZone.run(() => {
          this.callState = { ...this.callState, remoteStream: event.streams[0] };
          this.callStateChange.emit(this.callState);
        });
      };

      this.peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
          this.community.sendCallSignal(
            this.conversationId,
            JSON.stringify({ type: 'ICE', candidate: event.candidate, from: this.community.getCurrentEmail() }),
            callType
          ).subscribe();
        }
      };

      const offer = await this.peerConnection.createOffer();
      await this.peerConnection.setLocalDescription(offer);

      this.community.sendCallSignal(
        conversationId,
        JSON.stringify({ type: 'OFFER', offer, from: this.community.getCurrentEmail(), callType }),
        callType
      ).subscribe();

      this.ngZone.run(() => {
        this.callState = {
          active: true,
          callType,
          localStream: this.localStream,
          conversationId,
          incoming: false
        };
        this.callStateChange.emit(this.callState);
      });
    } catch (err) {
      console.error('Start call error', err);
      this.endCall();
      throw err;
    }
  }

  async acceptCall(): Promise<void> {
    if (!this.callState.conversationId) return;
    this._clearGuard();

    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: this.callState.callType === 'VIDEO'
      });

      this.peerConnection = new RTCPeerConnection(this.config);
      this.localStream.getTracks().forEach(t => this.peerConnection!.addTrack(t, this.localStream!));

      this.peerConnection.ontrack = (event) => {
        this.ngZone.run(() => {
          this.callState = { ...this.callState, remoteStream: event.streams[0] };
          this.callStateChange.emit(this.callState);
        });
      };

      this.peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
          this.community.sendCallSignal(
            this.callState.conversationId!,
            JSON.stringify({ type: 'ICE', candidate: event.candidate, from: this.community.getCurrentEmail() }),
            this.callState.callType
          ).subscribe();
        }
      };

      if (this.pendingOffer) {
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription(this.pendingOffer));
        const answer = await this.peerConnection.createAnswer();
        await this.peerConnection.setLocalDescription(answer);
        this.community.sendCallSignal(
          this.callState.conversationId!,
          JSON.stringify({ type: 'ANSWER', answer, from: this.community.getCurrentEmail() }),
          this.callState.callType
        ).subscribe();
        this.pendingOffer = undefined;
      }

      this.ngZone.run(() => {
        this.callState = { ...this.callState, localStream: this.localStream, incoming: false };
        this.callStateChange.emit(this.callState);
      });
    } catch (err) {
      this.endCall();
      throw err;
    }
  }

  async handleSignal(raw: string, conversationId?: string): Promise<void> {
    let parsed: any;
    try { parsed = typeof raw === 'string' ? JSON.parse(raw) : raw; } catch { return; }

    if (parsed.from === this.community.getCurrentEmail()) return;

    // Un OFFER entrant = appel entrant → stocker l'offre SDP en attendant que l'utilisateur accepte
    if (parsed.type === 'OFFER') {
      if (this._callGuard) return;
      this.pendingOffer = parsed.offer;
      if (!this.callState.active) {
        this.ngZone.run(() => {
          this.callState = {
            active: true,
            callType: parsed.callType || 'AUDIO',
            conversationId: conversationId || this.callState.conversationId || this.conversationId,
            remoteUserName: parsed.from,
            incoming: true
          };
          this.callStateChange.emit(this.callState);
        });
      }
      return;
    }

    if (parsed.type === 'ANSWER' && this.peerConnection) {
      await this.peerConnection.setRemoteDescription(new RTCSessionDescription(parsed.answer));
      return;
    }

    if (parsed.type === 'ICE' && parsed.candidate && this.peerConnection) {
      await this.peerConnection.addIceCandidate(new RTCIceCandidate(parsed.candidate));
      return;
    }

    if (parsed.type === 'CALL_DECLINED') {
      this.callDeclined$.next({
        callType: parsed.callType || this.callState.callType || 'AUDIO',
        byEmail: parsed.from
      });
      this._resetCallState();
      return;
    }

    if (parsed.type === 'CALL_ENDED') {
      this._resetCallState();
    }
  }

  declineCall(): void {
    const callType = this.callState.callType;
    const myEmail = this.community.getCurrentEmail();
    this._setGuard();
    this.pendingOffer = undefined;

    if (this.callState.conversationId) {
      this.community.sendCallSignal(
        this.callState.conversationId,
        JSON.stringify({ type: 'CALL_DECLINED', from: myEmail, callType }),
        callType
      ).subscribe({ error: () => {} });
    }

    this.callDeclined$.next({ callType, byEmail: myEmail });

    this.ngZone.run(() => {
      this.callState = { active: false, callType: 'AUDIO' };
      this.callStateChange.emit(this.callState);
    });
  }

  endCall(): void {
    this._setGuard();
    this._closePeerConnection();
    this._stopLocalStream();

    if (this.callState.conversationId) {
      this.community.sendCallSignal(
        this.callState.conversationId,
        JSON.stringify({ type: 'CALL_ENDED', from: this.community.getCurrentEmail() }),
        this.callState.callType
      ).subscribe({ error: () => {} });
    }

    this.conversationId = '';
    this.ngZone.run(() => {
      this.callState = { active: false, callType: 'AUDIO' };
      this.callStateChange.emit(this.callState);
    });
  }

  toggleMute(): boolean {
    if (!this.localStream) return false;
    const tracks = this.localStream.getAudioTracks();
    tracks.forEach(t => t.enabled = !t.enabled);
    return tracks.length > 0 ? !tracks[0].enabled : false;
  }

  toggleVideo(): boolean {
    if (!this.localStream) return false;
    const tracks = this.localStream.getVideoTracks();
    tracks.forEach(t => t.enabled = !t.enabled);
    return tracks.length > 0 ? !tracks[0].enabled : false;
  }

  // ─── helpers privés ───────────────────────────────────────────

  private _resetCallState(): void {
    this._setGuard();
    this._closePeerConnection();
    this._stopLocalStream();
    this.pendingOffer = undefined;
    this.conversationId = '';

    this.ngZone.run(() => {
      this.callState = { active: false, callType: 'AUDIO' };
      this.callStateChange.emit(this.callState);
    });
  }

  private _closePeerConnection(): void {
    if (!this.peerConnection) return;
    // Neutralise les callbacks AVANT close() pour éviter tout événement tardif
    this.peerConnection.ontrack = null;
    this.peerConnection.onicecandidate = null;
    this.peerConnection.onconnectionstatechange = null;
    this.peerConnection.oniceconnectionstatechange = null;
    this.peerConnection.close();
    this.peerConnection = undefined;
  }

  private _stopLocalStream(): void {
    this.localStream?.getTracks().forEach(t => t.stop());
    this.localStream = undefined;
  }

  private _setGuard(): void {
    this._callGuard = true;
    if (this._callGuardTimer) window.clearTimeout(this._callGuardTimer);
    this._callGuardTimer = window.setTimeout(() => { this._callGuard = false; }, 4000);
  }

  private _clearGuard(): void {
    this._callGuard = false;
    if (this._callGuardTimer) window.clearTimeout(this._callGuardTimer);
    this._callGuardTimer = undefined;
  }
}
