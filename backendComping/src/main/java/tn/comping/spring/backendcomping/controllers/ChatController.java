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
    private static final String ATTACHMENT_UPLOAD_DIR = "./uploads/chat";

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

    @PutMapping("/message/{messageId}")
    public ResponseEntity<MessageResponseDTO> updateMessage(
            @PathVariable String messageId,
            @RequestBody MessageRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(chatService.updateMessage(messageId, dto.getContenu(), auth.getName()));
    }

    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String messageId,
            Authentication auth) {
        chatService.deleteMessage(messageId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String conversationId,
            Authentication auth) {
        chatService.deleteConversation(conversationId, auth.getName());
        return ResponseEntity.noContent().build();
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

    @PostMapping(value = "/attachment", consumes = "multipart/form-data")
    public ResponseEntity<MessageResponseDTO> sendAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") String conversationId,
            Authentication auth) {
        try {
            String fileUrl = saveChatFile(file);
            String contentType = file.getContentType();
            String typeMessage = contentType != null && contentType.startsWith("image/") ? "IMAGE" : "FILE";

            MessageRequestDTO fileDto = MessageRequestDTO.builder()
                    .conversationId(conversationId)
                    .contenu(fileUrl)
                    .typeMessage(typeMessage)
                    .build();

            return ResponseEntity.ok(chatService.sendMessage(auth.getName(), fileDto));
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

        String fileName = UUID.randomUUID() + getSafeExtension(audio.getOriginalFilename(), ".wav");
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.copy(audio.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/voice/" + fileName;
    }

    private String saveChatFile(MultipartFile file) throws Exception {
        File uploadDirFile = new File(ATTACHMENT_UPLOAD_DIR);
        if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

        String fileName = UUID.randomUUID() + getSafeExtension(file.getOriginalFilename(), "");
        Path filePath = Paths.get(ATTACHMENT_UPLOAD_DIR, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/chat/" + fileName;
    }

    private String getSafeExtension(String originalName, String fallback) {
        if (originalName == null || !originalName.contains(".")) return fallback;
        String extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,8}") ? extension : fallback;
    }
}
