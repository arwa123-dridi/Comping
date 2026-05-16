# TODO - Module Chat Améliorations

## ✅ Implémenté par BlackboxAI
- [x] pom.xml: Ajout dependency Vosk 0.3.45
- [x] ChatController.java: Fix getUserIdFromAuth + saveAudioFile
- [x] ChatServiceImpl.java: Fix mappers noms users
- [x] application.properties: Config multipart uploads
- [x] Suppression handler disconnect inutile

## ⏳ Manuel (Utilisateur)
- [ ] **Télécharger** https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip
- [ ] **Décompresser** dans `src/main/resources/vosk-model-small-fr-0.22/`
- [ ] `mvn clean install`
- [ ] **Tester**:
  
```bash
  # 1. Status online (STOMP client)
  # 2. POST /api/chat/voice (Postman WAV 16kHz)
  # 3. WebRTC signal /api/chat/call/conv123/signal
  
```

**Features prêtes:**
- Status online (SimpUserRegistry)
- WebRTC signaling (P2P via STOMP)
- Voice + Vosk transcription (offline FR)
