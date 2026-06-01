package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.dto.ReactionRequestDTO;
import tn.comping.spring.backendcomping.services.PostService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import tn.comping.spring.backendcomping.services.AbonnementService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PostController {

    private final PostService postService;
    private final AbonnementService abonnementService;
    private final ObjectMapper objectMapper;
    private static final String POST_UPLOAD_DIR = "./uploads/posts";

    // === CRUD ===
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(postService.createPost(dto, authentication.getName()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPostWithImages(
            @RequestPart("post") String postJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) throws Exception {
        PostRequestDTO dto = objectMapper.readValue(postJson, PostRequestDTO.class);
        dto.setImages(savePostImages(images));
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

    @GetMapping("/amis")
    public ResponseEntity<List<PostResponseDTO>> getAmisPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        List<String> suiviIds = abonnementService.getMesAbonnements(authentication.getName())
                .stream()
                .map(a -> a.getSuiviId())
                .collect(Collectors.toList());
        return ResponseEntity.ok(postService.getFriendsPosts(suiviIds, page, size, authentication.getName()));
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

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable String id,
            @RequestBody PostRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(postService.updatePost(id, dto, authentication.getName()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> updatePostWithImages(
            @PathVariable String id,
            @RequestPart("post") String postJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages,
            Authentication authentication) throws Exception {
        PostRequestDTO dto = objectMapper.readValue(postJson, PostRequestDTO.class);
        List<String> uploaded = savePostImages(newImages);
        List<String> all = new ArrayList<>(dto.getImages() != null ? dto.getImages() : List.of());
        all.addAll(uploaded);
        dto.setImages(all);
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

    private List<String> savePostImages(List<MultipartFile> images) throws Exception {
        List<String> imageUrls = new ArrayList<>();
        if (images == null || images.isEmpty()) {
            return imageUrls;
        }

        File uploadDirFile = new File(POST_UPLOAD_DIR);
        if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) continue;

            String originalName = image.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }

            String fileName = UUID.randomUUID() + extension;
            Path filePath = Paths.get(POST_UPLOAD_DIR, fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageUrls.add("http://localhost:8087/uploads/posts/" + fileName);
        }

        return imageUrls;
    }
}
