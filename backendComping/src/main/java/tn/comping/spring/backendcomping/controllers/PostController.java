package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.dto.ReactionRequestDTO;
import tn.comping.spring.backendcomping.services.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PostController {

    private final PostService postService;

    // === CRUD ===
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(postService.createPost(dto, authentication.getName()));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostResponseDTO>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(postService.getFeedPosts(page, size, authentication.getName()));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<PostResponseDTO>> getTrending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(postService.getTrendingPosts(page, size, authentication.getName()));
    }

    @GetMapping("/hashtag/{hashtag}")
    public ResponseEntity<List<PostResponseDTO>> getByHashtag(
            @PathVariable String hashtag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(postService.getPostsByHashtag(hashtag, page, size, authentication.getName()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(postService.getUserPosts(userId, page, size, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(postService.getPostById(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable String id,
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(postService.updatePost(id, dto, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            Authentication authentication) {
        postService.deletePost(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // === REACTIONS ===
    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> likePost(
            @PathVariable String id,
            Authentication authentication) {
        postService.likePost(id, authentication.getName());
        return ResponseEntity.ok(postService.getPostById(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> unlikePost(
            @PathVariable String id,
            Authentication authentication) {
        postService.unlikePost(id, authentication.getName());
        return ResponseEntity.ok(postService.getPostById(id, authentication.getName()));
    }

    @PostMapping("/{id}/react")
    public ResponseEntity<PostResponseDTO> reactToPost(
            @PathVariable String id,
            @RequestBody ReactionRequestDTO dto,
            Authentication authentication) {
        postService.reactToPost(id, authentication.getName(), dto.getEmoji());
        return ResponseEntity.ok(postService.getPostById(id, authentication.getName()));
    }

    @DeleteMapping("/{id}/react")
    public ResponseEntity<PostResponseDTO> removeReaction(
            @PathVariable String id,
            Authentication authentication) {
        postService.removeReaction(id, authentication.getName());
        return ResponseEntity.ok(postService.getPostById(id, authentication.getName()));
    }
}
