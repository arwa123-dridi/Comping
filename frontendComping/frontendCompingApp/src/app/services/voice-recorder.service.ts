import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class VoiceRecorderService {
  private mediaRecorder?: MediaRecorder;
  private chunks: Blob[] = [];
  private stream?: MediaStream;

  async startRecording(): Promise<void> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.chunks = [];

      // WAV preferred but most browsers default to webm/opus
      const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : 'audio/webm';

      this.mediaRecorder = new MediaRecorder(this.stream, { mimeType });
      this.mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) this.chunks.push(e.data);
      };
      this.mediaRecorder.start();
    } catch (err) {
      throw new Error('Accès au microphone refusé ou non disponible');
    }
  }

  stopRecording(): Promise<Blob> {
    return new Promise((resolve, reject) => {
      if (!this.mediaRecorder) {
        reject(new Error('Pas d\'enregistrement en cours'));
        return;
      }
      this.mediaRecorder.onstop = async () => {
        const blob = new Blob(this.chunks, { type: this.mediaRecorder!.mimeType });
        this.releaseStream();
        // Convertir en WAV 16kHz mono pour Vosk
        try {
          const wavBlob = await this.convertToWav16k(blob);
          resolve(wavBlob);
        } catch {
          resolve(blob); // fallback: blob original
        }
      };
      this.mediaRecorder.stop();
    });
  }

  cancelRecording(): void {
    if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
      this.mediaRecorder.stop();
    }
    this.releaseStream();
    this.chunks = [];
  }

  isRecording(): boolean {
    return this.mediaRecorder?.state === 'recording';
  }

  private releaseStream(): void {
    this.stream?.getTracks().forEach(t => t.stop());
    this.stream = undefined;
  }

  /**
   * Convertit un blob audio en WAV 16kHz mono (compatible Vosk)
   */
  async convertToWav16k(blob: Blob): Promise<Blob> {
    const arrayBuffer = await blob.arrayBuffer();
    const AudioContextCtor = window.AudioContext || (window as any).webkitAudioContext;
    const audioCtx = new AudioContextCtor();
    const audioBuffer = await audioCtx.decodeAudioData(arrayBuffer);

    // Récupérer le canal mono (mix down si stéréo)
    const numChannels = 1;
    const sampleRate = 16000;
    const targetLength = Math.max(1, Math.ceil(audioBuffer.duration * sampleRate));
    const offlineCtx = new OfflineAudioContext(numChannels, targetLength, sampleRate);
    const source = offlineCtx.createBufferSource();
    source.buffer = audioBuffer;
    source.connect(offlineCtx.destination);
    source.start(0);
    const resampled = await offlineCtx.startRendering();
    const mono = resampled.getChannelData(0);
    const length = mono.length;

    // Encoder en WAV PCM 16-bit
    const buffer = new ArrayBuffer(44 + length * 2);
    const view = new DataView(buffer);

    const writeString = (offset: number, str: string) => {
      for (let i = 0; i < str.length; i++) view.setUint8(offset + i, str.charCodeAt(i));
    };

    writeString(0, 'RIFF');
    view.setUint32(4, 36 + length * 2, true);
    writeString(8, 'WAVE');
    writeString(12, 'fmt ');
    view.setUint32(16, 16, true); // PCM size
    view.setUint16(20, 1, true);  // PCM format
    view.setUint16(22, numChannels, true);
    view.setUint32(24, sampleRate, true);
    view.setUint32(28, sampleRate * numChannels * 2, true);
    view.setUint16(32, numChannels * 2, true);
    view.setUint16(34, 16, true);
    writeString(36, 'data');
    view.setUint32(40, length * 2, true);

    // PCM data
    let offset = 44;
    for (let i = 0; i < length; i++, offset += 2) {
      const s = Math.max(-1, Math.min(1, mono[i]));
      view.setInt16(offset, s < 0 ? s * 0x8000 : s * 0x7FFF, true);
    }

    await audioCtx.close();
    return new Blob([buffer], { type: 'audio/wav' });
  }
}
