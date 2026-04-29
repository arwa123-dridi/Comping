package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.services.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        String userId = authentication.getName(); // Extract properly
        return ResponseEntity.ok(postService.createPost(dto, userId));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostResponseDTO>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(postService.getFeedPosts(page, size));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponseDTO>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getUserPosts(userId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable String id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable String id,
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(postService.updatePost(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> likePost(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        postService.likePost(id, userId);
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> unlikePost(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication.getName();
        postService.unlikePost(id, userId);
        return ResponseEntity.ok(postService.getPostById(id));
    }
}
