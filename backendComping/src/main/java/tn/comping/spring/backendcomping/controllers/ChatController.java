package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.ChatService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;
    
    private static final String UPLOAD_DIR = "./uploads/voice";

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(Authentication auth) {
        return ResponseEntity.ok(chatService.getUserConversations(auth.getName()));
    }

    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponseDTO> getOrCreateConversation(
            @RequestBody ConversationRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(chatService.getOrCreateConversation(auth.getName(), dto));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size, auth.getName()));
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @RequestBody MessageRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(chatService.sendMessage(auth.getName(), dto));
    }

    // === NOUVEAUX ENDPOINTS ===
    
    @PostMapping(value = "/voice", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponseDTO> sendVoiceMessage(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("conversationId") String conversationId,
            Authentication auth) {
        try {
            String userId = auth.getName();
            String transcription = chatService.transcribeVoice(audio.getBytes());
            
            // Save audio file (uploads/voice/)
            String audioUrl = saveAudioFile(audio);
            
            MessageRequestDTO voiceDto = new MessageRequestDTO();
            voiceDto.setConversationId(conversationId);
            voiceDto.setContenu(audioUrl);
            voiceDto.setTypeMessage("VOICE");
            voiceDto.setTranscription(transcription);
            
            MessageResponseDTO voiceMsg = chatService.sendMessage(userId, voiceDto);
            return ResponseEntity.ok(voiceMsg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/call/{conversationId}/signal")
    public ResponseEntity<Void> sendCallSignal(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "VIDEO") String callType,
            @RequestBody String signalData,
            Authentication auth) {
        chatService.handleCallSignal(conversationId, signalData, auth.getName(), callType);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/messages/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String conversationId,
            Authentication auth) {
        chatService.markAsRead(conversationId, auth.getName());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/status/{userId}")
    public ResponseEntity<Boolean> getUserStatus(@PathVariable String userId) {
        return ResponseEntity.ok(chatService.isUserOnline(userId));
    }

    private String getUserIdFromAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Utilisateur non authentifié");
        }
        // Utilise auth.getName() (JWT subject = userId ou email)
        return auth.getName();
    }
    
    private String saveAudioFile(MultipartFile audio) throws IOException {
        // Créer dossier si n'existe pas
        File uploadDirFile = new File(UPLOAD_DIR);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        
        // UUID + extension .wav (Vosk attend 16kHz mono WAV)
        String fileName = UUID.randomUUID() + ".wav";
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        
        // Copy fichier
        Files.copy(audio.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return "/uploads/voice/" + fileName;
    }

}
