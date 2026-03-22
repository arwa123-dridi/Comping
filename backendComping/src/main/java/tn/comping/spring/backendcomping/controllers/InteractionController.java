package tn.comping.spring.backendcomping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.InteractionRequestDTO;
import tn.comping.spring.backendcomping.dto.InteractionResponseDTO;
import tn.comping.spring.backendcomping.entities.CibleType;
import tn.comping.spring.backendcomping.services.InteractionService;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "Interactions", description = "PHASE 1 - Likes et Commentaires unifiés (Avis/Post)")
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/like")
    @Operation(summary = "Liker un avis ou post", description = "Crée un LIKE (unicité garantie)")
    public ResponseEntity<InteractionResponseDTO> like(@Valid @RequestBody InteractionRequestDTO dto, 
                                                      Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(interactionService.creerLike(dto, email));
    }

    @PostMapping("/commentaire")
    @Operation(summary = "Commenter un avis ou post", description = "Crée un COMMENTAIRE (max 1000 chars)")
    public ResponseEntity<InteractionResponseDTO> commentaire(@Valid @RequestBody InteractionRequestDTO dto, 
                                                             Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(interactionService.creerCommentaire(dto, email));
    }

    @GetMapping("/likes")
    @Operation(summary = "Récupérer les likes d'une cible", description = "Liste utilisateurs ayant liké")
    public ResponseEntity<List<InteractionResponseDTO>> getLikes(
            @RequestParam CibleType cibleType,
            @RequestParam String cibleId) {
        return ResponseEntity.ok(interactionService.getLikes(cibleType, cibleId));
    }

    @GetMapping("/commentaires")
    @Operation(summary = "Récupérer les commentaires d'une cible", description = "Triés par date DESC")
    public ResponseEntity<List<InteractionResponseDTO>> getCommentaires(
            @RequestParam CibleType cibleType,
            @RequestParam String cibleId) {
        return ResponseEntity.ok(interactionService.getCommentaires(cibleType, cibleId));
    }

    @DeleteMapping("/like/{id}")
    @Operation(summary = "Supprimer son LIKE")
    public ResponseEntity<Void> supprimerLike(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        interactionService.supprimerLike(id, email);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/commentaire/{id}")
    @Operation(summary = "Supprimer son COMMENTAIRE")
    public ResponseEntity<Void> supprimerCommentaire(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        interactionService.supprimerCommentaire(id, email);
        return ResponseEntity.noContent().build();
    }
}

