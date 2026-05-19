package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.services.PostService;
import tn.comping.spring.backendcomping.utils.mapper.PostMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final AbonnementRepository abonnementRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto, String userId) {
        validateUser(userId);
        validatePostPayload(dto);

        List<String> hashtags = extractHashtags(dto.getContenu());

        Post post = Post.builder()
                .auteurId(userId)
                .typePost("FEED")
                .contenu(dto.getContenu().trim())
                .images(dto.getImages() != null ? dto.getImages() : List.of())
                .datePublication(new Date())
                .likesCount(0)
                .commentairesCount(0)
                .reactions(new HashMap<>())
                .hashtags(hashtags)
                .visibilite(dto.getVisibilite() != null ? dto.getVisibilite() : "PUBLIC")
                .build();

        Post saved = postRepository.save(post);
        log.info("Post créé - ID: {}, Auteur: {}", saved.getId(), userId);

        String auteurNom = signupRepository.findById(userId)
                .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                .orElse("Un campeur");

        abonnementRepository.findBySuiviId(userId).forEach(abonnement -> {
            notificationService.createNotification(abonnement.getSuiveurId(), userId, "NEW_POST", saved.getId(), auteurNom + " a publié un nouveau post.");
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "NEW_POST");
            payload.put("expediteurNom", auteurNom);
            payload.put("postId", saved.getId());
            messagingTemplate.convertAndSend("/topic/user/" + abonnement.getSuiveurId() + "/notifications", payload);
        });

        return postMapper.toResponseDTO(saved, userId);
    }

    @Override
    public List<PostResponseDTO> getFeedPosts(int page, int size, String currentUserId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("datePublication").descending());
        List<String> followingIds = abonnementRepository.findBySuiveurId(currentUserId)
                .stream().map(Abonnement::getSuiviId).collect(Collectors.toList());
        
        followingIds.add(currentUserId);

        return postRepository.findByAuteurIdInOrderByDatePublicationDesc(followingIds, pageable).stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getUserPosts(String userId, int page, int size, String currentUserId) {
        return postRepository.findByAuteurIdOrderByDatePublicationDesc(
                        userId, PageRequest.of(page, size))
                .stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(String id, String currentUserId) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return postMapper.toResponseDTO(post, currentUserId);
    }

    @Override
    public PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!post.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        post.setContenu(dto.getContenu());
        post.setImages(dto.getImages());
        return postMapper.toResponseDTO(postRepository.save(post), userId);
    }

    @Override
    public void deletePost(String id, String userId) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!post.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        postRepository.deleteById(id);
    }

    @Override
    public void likePost(String postId, String userId) {
        reactToPost(postId, userId, "👍");
    }

    @Override
    public void unlikePost(String postId, String userId) {
        removeReaction(postId, userId);
    }

    @Override
    public void reactToPost(String postId, String userId, String emoji) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "POST", postId, "REACTION")
                .ifPresent(interactionRepository::delete);

        Interaction interaction = Interaction.builder()
                .auteurId(userId)
                .cibleType("POST")
                .cibleId(postId)
                .type("REACTION")
                .emoji(emoji)
                .dateCreation(new Date())
                .build();
        interactionRepository.save(interaction);

        if ("👍".equals(emoji)) {
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
        }

        if (!post.getAuteurId().equals(userId)) {
            notificationService.createNotification(post.getAuteurId(), userId, "REACTION", postId, " a réagi à votre post.");
        }
    }

    @Override
    public void removeReaction(String postId, String userId) {
        interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "POST", postId, "REACTION")
                .ifPresent(interaction -> {
                    if ("👍".equals(interaction.getEmoji())) {
                        postRepository.findById(postId).ifPresent(p -> {
                            p.setLikesCount(Math.max(0, p.getLikesCount() - 1));
                            postRepository.save(p);
                        });
                    }
                    interactionRepository.delete(interaction);
                });
    }

    @Override
    public List<PostResponseDTO> getTrendingPosts(int page, int size, String currentUserId) {
        // Simple implementation for trending: most liked recent posts
        PageRequest pageable = PageRequest.of(page, size, Sort.by("likesCount").descending().and(Sort.by("datePublication").descending()));
        return postRepository.findAll(pageable).stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostsByHashtag(String hashtag, int page, int size, String currentUserId) {
        return postRepository.findByHashtagsContainingIgnoreCase(hashtag, PageRequest.of(page, size))
                .stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getFriendsPosts(List<String> suiviIds, int page, int size, String currentUserId) {
        return postRepository.findByAuteurIdInOrderByDatePublicationDesc(suiviIds, PageRequest.of(page, size))
                .stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public void recalculateTrendScores() {
        log.info("Recalculating trend scores...");
    }

    private void validateUser(String userId) {
        if (!signupRepository.existsById(userId) && !signupRepository.existsByEmail(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
    }

    private void validatePostPayload(PostRequestDTO dto) {
        if (dto.getContenu() == null || dto.getContenu().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu ne peut pas être vide");
        }
    }

    private List<String> extractHashtags(String text) {
        List<String> tags = new ArrayList<>();
        if (text == null) return tags;
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }
}
