package tn.comping.spring.backendcomping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import tn.comping.spring.backendcomping.services.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "PHASE 3 - Messagerie privée")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    @Operation(summary = "Créer conversation 1:1")
    public ResponseEntity<ConversationResponseDTO> creerConversation(
            @RequestParam String autreUserId,
            @RequestParam(required = false) String avisId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(chatService.creerConversation(autreUserId, email, avisId));
    }

    @PostMapping("/conversations/depuis-avis/{avisId}")
    @Operation(summary = "Conversation depuis avis (propriétaire)")
    public ResponseEntity<ConversationResponseDTO> creerConversationDepuisAvis(
            @PathVariable String avisId, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(chatService.creerConversationDepuisAvis(avisId, email));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Mes conversations")
    public ResponseEntity<List<ConversationResponseDTO>> getConversations(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(chatService.getMesConversations(email));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Détail conversation")
    public ResponseEntity<ConversationResponseDTO> getConversation(@PathVariable String id) {
        // TODO implémenter
        return ResponseEntity.ok(null);
    }

    @PostMapping("/conversations/{id}/messages")
    @Operation(summary = "Envoyer message")
    public ResponseEntity<MessageResponseDTO> envoyerMessage(@PathVariable String id,
                                                            @Valid @RequestBody MessageRequestDTO dto,
                                                            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(chatService.envoyerMessage(id, dto, email));
    }

    @GetMapping("/conversations/{id}/messages")
    @Operation(summary = "Historique messages")
    public ResponseEntity<List<MessageResponseDTO>> getMessages(@PathVariable String id) {
        return ResponseEntity.ok(chatService.getMessagesConversation(id));
    }

    @PutMapping("/conversations/{id}/marquer-comme-lu")
    @Operation(summary = "Marquer messages lus")
    public ResponseEntity<Void> marquerLu(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        chatService.marquerLu(id, email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Supprimer conversation")
    public ResponseEntity<Void> supprimerConversation(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        chatService.supprimerConversation(id, email);
        return ResponseEntity.noContent().build();
    }
}

