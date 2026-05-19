package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.ChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@CrossOrigin("*")
public class ChatController {

    private final ChatService chatService;

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

    @PostMapping("/group")
    public ResponseEntity<ConversationResponseDTO> createGroup(
            @RequestBody GroupConversationRequestDTO dto,
            Authentication auth) {
        return ResponseEntity.ok(chatService.createGroupConversation(auth.getName(), dto));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size, auth.getName()));
    }

    @PostMapping("/voice")
    public ResponseEntity<Map<String, String>> uploadVoice(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(chatService.uploadVoiceMessage(file));
    }

    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId, Authentication auth) {
        chatService.deleteMessage(messageId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
