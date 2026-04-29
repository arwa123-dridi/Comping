package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.services.CommentaireService;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommentaireController {

    private final CommentaireService commentaireService;

    @PostMapping
    public ResponseEntity<CommentaireResponseDTO> createComment(
            @PathVariable String postId,
            @RequestBody CommentaireRequestDTO dto,
            Authentication authentication) {
        dto.setPostId(postId);
        String userId = authentication.getName();
        return ResponseEntity.ok(commentaireService.createComment(dto, userId));
    }

    @GetMapping
    public ResponseEntity<List<CommentaireResponseDTO>> getComments(@PathVariable String postId) {
        return ResponseEntity.ok(commentaireService.getCommentairesByPost(postId));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentaireResponseDTO> getComment(
            @PathVariable String postId,
            @PathVariable String commentId) {
        return ResponseEntity.ok(commentaireService.getCommentById(commentId));
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<CommentaireResponseDTO> replyToComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestBody CommentaireRequestDTO dto,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(commentaireService.replyToComment(commentId, dto, userId));
    }
}
