package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.PostService;
import tn.comping.spring.backendcomping.utils.mapper.PostMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final InteractionRepository interactionRepository;
    private final SignupRepository signupRepository;
    private final PostMapper postMapper;
    private final CommentaireRepository commentaireRepository;

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto, String userId) {
        validateUser(userId);
        validatePostPayload(dto);

        Post post = Post.builder()
                .auteurId(userId)
                .typePost("FEED")
                .avisId(dto.getAvisId())
                .cibleType(dto.getCibleType())
                .cibleId(dto.getCibleId())
                .contenu(dto.getContenu().trim())
                .images(dto.getImages() != null ? dto.getImages() : List.of())
                .datePublication(new Date())
                .likesCount(0)
                .commentairesCount(0)
                .build();

        Post saved = postRepository.save(post);
        log.info("Post cree - ID: {}, Auteur: {}", saved.getId(), userId);
        return postMapper.toResponseDTO(saved);
    }

    @Override
    public List<PostResponseDTO> getFeedPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by("datePublication").descending());
        return postRepository.findAll(pageable).stream()
                .map(postMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getUserPosts(String userId, int page, int size) {
        validateUser(userId);
        return postRepository.findByAuteurIdOrderByDatePublicationDesc(
                        userId,
                        PageRequest.of(safePage(page), safeSize(size)))
                .stream()
                .map(postMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(String id) {
        Post post = getPostOrThrow(id);
        return postMapper.toResponseDTO(post);
    }

    @Override
    public PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId) {
        Post post = getPostOrThrow(id);
        validatePostPayload(dto);

        if (!post.getAuteurId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorise");
        }

        post.setContenu(dto.getContenu().trim());
        post.setImages(dto.getImages() != null ? dto.getImages() : List.of());
        Post updated = postRepository.save(post);
        return postMapper.toResponseDTO(updated);
    }

    @Override
    public void deletePost(String id, String userId) {
        Post post = getPostOrThrow(id);

        if (!post.getAuteurId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorise");
        }

        commentaireRepository.deleteByPostId(id);
        interactionRepository.deleteByCibleTypeAndCibleId("POST", id);
        postRepository.deleteById(id);
    }

    @Override
    public void likePost(String postId, String userId) {
        validateUser(userId);
        Post post = getPostOrThrow(postId);
        if (interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "POST", postId, "LIKE").isPresent()) {
            return;
        }

        interactionRepository.save(Interaction.builder()
                .auteurId(userId)
                .cibleType("POST")
                .cibleId(postId)
                .type("LIKE")
                .build());
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Override
    public void unlikePost(String postId, String userId) {
        validateUser(userId);
        Post post = getPostOrThrow(postId);
        var interaction = interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "POST", postId, "LIKE");
        if (interaction.isEmpty()) {
            return;
        }

        interactionRepository.delete(interaction.get());
        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
        }
        postRepository.save(post);
    }

    private Post getPostOrThrow(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouve"));
    }

    private void validatePostPayload(PostRequestDTO dto) {
        if (dto == null || dto.getContenu() == null || dto.getContenu().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu du post est obligatoire");
        }
    }

    private void validateUser(String userId) {
        signupRepository.findByEmail(userId)
                .or(() -> signupRepository.findById(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }
}
