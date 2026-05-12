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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;
    private static final String UPLOAD_DIR = "./uploads/voice";

    // === CONVERSATIONS ===
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

    // === GROUPES ===
    @PostMapping("/group")
    public ResponseEntity<ConversationResponseDTO> createGroup(
            @RequestBody GroupConversationRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(chatService.createGroupConversation(auth.getName(), dto));
    }

    @PostMapping("/group/{conversationId}/add")
    public ResponseEntity<ConversationResponseDTO> addParticipant(
            @PathVariable String conversationId,
            @RequestParam String participantId,
            Authentication auth) {
        return ResponseEntity.ok(chatService.addParticipantToGroup(conversationId, auth.getName(), participantId));
    }

    @PostMapping("/group/{conversationId}/remove")
    public ResponseEntity<ConversationResponseDTO> removeParticipant(
            @PathVariable String conversationId,
            @RequestParam String participantId,
            Authentication auth) {
        return ResponseEntity.ok(chatService.removeParticipantFromGroup(conversationId, auth.getName(), participantId));
    }

    // === MESSAGES ===
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

    @PutMapping("/messages/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String conversationId,
            Authentication auth) {
        chatService.markAsRead(conversationId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // === VOICE MESSAGES ===
    @PostMapping(value = "/voice", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponseDTO> sendVoiceMessage(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("conversationId") String conversationId,
            Authentication auth) {
        try {
            String transcription = chatService.transcribeVoice(audio.getBytes());
            String audioUrl = saveAudioFile(audio);

            MessageRequestDTO voiceDto = MessageRequestDTO.builder()
                    .conversationId(conversationId)
                    .contenu(audioUrl)
                    .typeMessage("VOICE")
                    .transcription(transcription)
                    .build();

            return ResponseEntity.ok(chatService.sendMessage(auth.getName(), voiceDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === VIDEO/AUDIO CALLS ===
    @PostMapping("/call/{conversationId}/signal")
    public ResponseEntity<Void> sendCallSignal(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "VIDEO") String callType,
            @RequestBody String signalData,
            Authentication auth) {
        chatService.handleCallSignal(conversationId, signalData, auth.getName(), callType);
        return ResponseEntity.ok().build();
    }

    // === STATUT UTILISATEUR ===
    @GetMapping("/status/{userId}")
    public ResponseEntity<UserStatusDTO> getUserStatus(@PathVariable String userId) {
        return ResponseEntity.ok(chatService.getUserStatus(userId));
    }

    @GetMapping("/online")
    public ResponseEntity<List<UserStatusDTO>> getOnlineUsers() {
        return ResponseEntity.ok(chatService.getOnlineUsers());
    }

    // === HELPER ===
    private String saveAudioFile(MultipartFile audio) throws Exception {
        File uploadDirFile = new File(UPLOAD_DIR);
        if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

        String fileName = UUID.randomUUID() + ".wav";
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.copy(audio.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/voice/" + fileName;
    }
}
