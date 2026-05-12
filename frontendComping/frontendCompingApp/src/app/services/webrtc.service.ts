import { Injectable, EventEmitter } from '@angular/core';
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

@Injectable({ providedIn: 'root' })
export class WebRtcService {
  callState: CallState = { active: false, callType: 'VIDEO' };
  callStateChange = new EventEmitter<CallState>();

  private peerConnection?: RTCPeerConnection;
  private localStream?: MediaStream;
  private conversationId = '';
  private callType: 'AUDIO' | 'VIDEO' = 'VIDEO';

  private readonly config: RTCConfiguration = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' }
    ]
  };

  constructor(private community: CommunityService) {
    // Écouter les signaux WebRTC entrants via WebSocket
    this.community.notifications$.subscribe(notif => {
      if (notif.type === 'INCOMING_CALL' && notif.conversationId && notif.from !== this.community.getCurrentEmail()) {
        this.callState = {
          active: true,
          callType: notif.callType || 'VIDEO',
          conversationId: notif.conversationId,
          remoteUserName: notif.from,
          incoming: true
        };
        this.callStateChange.emit(this.callState);
      }
    });
  }

  async startCall(conversationId: string, callType: 'AUDIO' | 'VIDEO'): Promise<void> {
    this.conversationId = conversationId;
    this.callType = callType;

    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: callType === 'VIDEO'
      });

      this.peerConnection = new RTCPeerConnection(this.config);
      this.localStream.getTracks().forEach(t => this.peerConnection!.addTrack(t, this.localStream!));

      this.peerConnection.ontrack = (event) => {
        this.callState.remoteStream = event.streams[0];
        this.callStateChange.emit(this.callState);
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

      this.callState = {
        active: true,
        callType,
        localStream: this.localStream,
        conversationId,
        incoming: false
      };
      this.callStateChange.emit(this.callState);
    } catch (err) {
      console.error('Start call error', err);
      this.endCall();
      throw err;
    }
  }

  async acceptCall(): Promise<void> {
    // Simple flow: accept = ouvrir le stream local + envoyer answer si offer reçue
    if (!this.callState.conversationId) return;
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: true,
        video: this.callState.callType === 'VIDEO'
      });
      this.callState.localStream = this.localStream;
      this.callState.incoming = false;
      this.callStateChange.emit(this.callState);
    } catch (err) {
      this.endCall();
      throw err;
    }
  }

  endCall(): void {
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = undefined;
    }
    this.localStream?.getTracks().forEach(t => t.stop());
    this.localStream = undefined;

    if (this.callState.conversationId) {
      this.community.sendCallSignal(
        this.callState.conversationId,
        JSON.stringify({ type: 'CALL_ENDED', from: this.community.getCurrentEmail() }),
        this.callState.callType
      ).subscribe({ error: () => {} });
    }

    this.callState = { active: false, callType: 'VIDEO' };
    this.callStateChange.emit(this.callState);
  }

  toggleMute(): boolean {
    if (!this.localStream) return false;
    const audioTracks = this.localStream.getAudioTracks();
    audioTracks.forEach(t => t.enabled = !t.enabled);
    return audioTracks.length > 0 ? !audioTracks[0].enabled : false;
  }

  toggleVideo(): boolean {
    if (!this.localStream) return false;
    const videoTracks = this.localStream.getVideoTracks();
    videoTracks.forEach(t => t.enabled = !t.enabled);
    return videoTracks.length > 0 ? !videoTracks[0].enabled : false;
  }
}
