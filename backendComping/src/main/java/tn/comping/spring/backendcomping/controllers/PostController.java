package tn.comping.spring.backendcomping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.services.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "PHASE 2 - Réseau social posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Créer un post")
    public ResponseEntity<PostResponseDTO> creerPost(@Valid @RequestBody PostRequestDTO dto, 
                                                   Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(postService.creerPost(dto, email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier son post")
    public ResponseEntity<PostResponseDTO> modifierPost(@PathVariable String id, 
                                                       @Valid @RequestBody PostRequestDTO dto, 
                                                       Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(postService.modifierPost(id, dto, email));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer son post")
    public ResponseEntity<Void> supprimerPost(@PathVariable String id, Authentication authentication) {
        String email = authentication.getName();
        postService.supprimerPost(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/utilisateur/{userId}")
    @Operation(summary = "Posts d'un utilisateur")
    public ResponseEntity<List<PostResponseDTO>> getPostsUtilisateur(@PathVariable String userId) {
        return ResponseEntity.ok(postService.getPostsUtilisateur(userId));
    }

    @GetMapping("/publics")
    @Operation(summary = "Posts publics récents")
    public ResponseEntity<List<PostResponseDTO>> getPostsPublics() {
        return ResponseEntity.ok(postService.getPostsPublics());
    }

    @PostMapping("/partager-avis/{avisId}")
    @Operation(summary = "Partager un avis comme post")
    public ResponseEntity<PostResponseDTO> partagerAvis(@PathVariable String avisId, 
                                                       Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(postService.partagerAvis(avisId, email));
    }
}

