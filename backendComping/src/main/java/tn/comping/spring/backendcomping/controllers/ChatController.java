package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(Authentication auth) {
        String userId = getUserIdFromAuth(auth); // Impl extract
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponseDTO> getOrCreateConversation(
            @RequestBody ConversationRequestDTO dto,
            Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        return ResponseEntity.ok(chatService.getOrCreateConversation(userId, dto));
    }

    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(chatService.getMessages(conversationId, page, size));
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @RequestBody MessageRequestDTO dto,
            Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        return ResponseEntity.ok(chatService.sendMessage(userId, dto));
    }

    private String getUserIdFromAuth(Authentication auth) {
        // Extract userId from JWT email -> lookup SignupRepository
        return "user123"; // Placeholder, impl properly
    }

}
