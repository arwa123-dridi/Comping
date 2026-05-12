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

    // =========================================================
    // CRUD
    // =========================================================

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto, String userId) {
        validateUser(userId);
        validatePostPayload(dto);

        List<String> hashtags = extractHashtags(dto.getContenu());

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
                .reactions(new HashMap<>())
                .hashtags(hashtags)
                .trendScore(0.0)
                .visibilite(dto.getVisibilite() != null ? dto.getVisibilite() : "PUBLIC")
                .build();

        Post saved = postRepository.save(post);
        log.info("Post créé - ID: {}, Auteur: {}, Hashtags: {}", saved.getId(), userId, hashtags);
        return postMapper.toResponseDTO(saved, userId);
    }

    @Override
    public List<PostResponseDTO> getFeedPosts(int page, int size, String currentUserId) {
        PageRequest pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by("datePublication").descending());
        String currentUserKey = resolveExistingUserKey(currentUserId);
        return postRepository.findAll(pageable).stream()
                .filter(post -> "PUBLIC".equals(post.getVisibilite()))
                .map(post -> postMapper.toResponseDTO(post, currentUserKey))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getUserPosts(String userId, int page, int size, String currentUserId) {
        validateUser(userId);
        String currentUserKey = resolveExistingUserKey(currentUserId);
        return postRepository.findByAuteurIdOrderByDatePublicationDesc(
                        userId, PageRequest.of(safePage(page), safeSize(size)))
                .stream()
                .map(post -> postMapper.toResponseDTO(post, currentUserKey))
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(String id, String currentUserId) {
        Post post = getPostOrThrow(id);
        return postMapper.toResponseDTO(post, resolveExistingUserKey(currentUserId));
    }

    @Override
    public PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId) {
        Post post = getPostOrThrow(id);
        validatePostPayload(dto);

        if (!post.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");

        post.setContenu(dto.getContenu().trim());
        post.setImages(dto.getImages() != null ? dto.getImages() : List.of());
        post.setHashtags(extractHashtags(dto.getContenu()));
        Post updated = postRepository.save(post);
        return postMapper.toResponseDTO(updated, userId);
    }

    @Override
    public void deletePost(String id, String userId) {
        Post post = getPostOrThrow(id);
        if (!post.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");

        commentaireRepository.deleteByPostId(id);
        interactionRepository.deleteByCibleTypeAndCibleId("POST", id);
        postRepository.deleteById(id);
    }

    // =========================================================
    // REACTIONS (emoji)
    // =========================================================

    @Override
    public void likePost(String postId, String userId) {
        reactToPost(postId, userId, "👍"); // Like = 👍 par défaut
    }

    @Override
    public void unlikePost(String postId, String userId) {
        removeReaction(postId, userId);
    }

    @Override
    public void reactToPost(String postId, String userId, String emoji) {
        validateUser(userId);
        Post post = getPostOrThrow(postId);

        // Retirer l'ancienne réaction si existe
        var existingInteraction = interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                userId, "POST", postId, "REACTION");
        if (existingInteraction.isPresent()) {
            Interaction old = existingInteraction.get();
            String oldEmoji = old.getEmoji();
            if (oldEmoji != null && post.getReactions().containsKey(oldEmoji)) {
                post.getReactions().put(oldEmoji, Math.max(0, post.getReactions().get(oldEmoji) - 1));
                if (post.getReactions().get(oldEmoji) == 0) post.getReactions().remove(oldEmoji);
            }
            if ("👍".equals(oldEmoji)) post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            interactionRepository.delete(old);
        }

        // Ajouter nouvelle réaction
        Interaction interaction = Interaction.builder()
                .auteurId(userId)
                .cibleType("POST")
                .cibleId(postId)
                .type("REACTION")
                .emoji(emoji)
                .dateCreation(new Date())
                .build();
        interactionRepository.save(interaction);

        post.getReactions().put(emoji, post.getReactions().getOrDefault(emoji, 0) + 1);
        if ("👍".equals(emoji)) post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Override
    public void removeReaction(String postId, String userId) {
        validateUser(userId);
        Post post = getPostOrThrow(postId);

        var interaction = interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                userId, "POST", postId, "REACTION");
        if (interaction.isEmpty()) return;

        Interaction inter = interaction.get();
        String emoji = inter.getEmoji();
        if (emoji != null && post.getReactions().containsKey(emoji)) {
            post.getReactions().put(emoji, Math.max(0, post.getReactions().get(emoji) - 1));
            if (post.getReactions().get(emoji) == 0) post.getReactions().remove(emoji);
        }
        if ("👍".equals(emoji) && post.getLikesCount() > 0) post.setLikesCount(post.getLikesCount() - 1);

        interactionRepository.delete(inter);
        postRepository.save(post);
    }

    // =========================================================
    // TRENDING & IA
    // =========================================================

    @Override
    public List<PostResponseDTO> getTrendingPosts(int page, int size, String currentUserId) {
        PageRequest pageable = PageRequest.of(safePage(page), safeSize(size), Sort.by("trendScore").descending());
        String currentUserKey = resolveExistingUserKey(currentUserId);
        return postRepository.findAll(pageable).stream()
                .filter(post -> "PUBLIC".equals(post.getVisibilite()))
                .filter(post -> post.getTrendScore() > 0)
                .map(post -> postMapper.toResponseDTO(post, currentUserKey))
                .collect(Collectors.toList());
    }

    @Override
    public List<PostResponseDTO> getPostsByHashtag(String hashtag, int page, int size, String currentUserId) {
        String currentUserKey = resolveExistingUserKey(currentUserId);
        String cleanHashtag = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;

        return postRepository.findAll(PageRequest.of(safePage(page), safeSize(size),
                        Sort.by("datePublication").descending()))
                .stream()
                .filter(post -> post.getHashtags() != null && post.getHashtags().contains(cleanHashtag))
                .filter(post -> "PUBLIC".equals(post.getVisibilite()))
                .map(post -> postMapper.toResponseDTO(post, currentUserKey))
                .collect(Collectors.toList());
    }

    @Override
    public void recalculateTrendScores() {
        List<Post> allPosts = postRepository.findAll();
        Date now = new Date();

        for (Post post : allPosts) {
            double score = calculateTrendScore(post, now);
            post.setTrendScore(score);
            postRepository.save(post);
        }
        log.info("Recalculé {} scores de tendance", allPosts.size());
    }

    /**
     * Score IA simple:
     * - Récence: posts récents = score plus élevé
     * - Interactions: likes + commentaires * 2 + reactions * 1.5
     * - Hashtags tendances: bonus si hashtag populaire
     */
    private double calculateTrendScore(Post post, Date now) {
        if (post.getDatePublication() == null) return 0.0;

        // Récence (max 100 points si < 1h)
        long ageHours = (now.getTime() - post.getDatePublication().getTime()) / (1000 * 60 * 60);
        double recencyScore = Math.max(0, 100 - (ageHours * 2)); // décroît de 2 points par heure

        // Interactions
        int totalReactions = post.getReactions() != null
                ? post.getReactions().values().stream().mapToInt(Integer::intValue).sum()
                : 0;
        double interactionScore = (post.getLikesCount() * 1.0)
                + (post.getCommentairesCount() * 2.0)
                + (totalReactions * 1.5);

        // Hashtags (bonus si hashtag fréquent)
        double hashtagBonus = post.getHashtags() != null ? post.getHashtags().size() * 5.0 : 0;

        return recencyScore + interactionScore + hashtagBonus;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private List<String> extractHashtags(String contenu) {
        if (contenu == null) return List.of();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(contenu);
        List<String> hashtags = new ArrayList<>();
        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }
        return hashtags;
    }

    private Post getPostOrThrow(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouvé"));
    }

    private void validatePostPayload(PostRequestDTO dto) {
        if (dto == null || dto.getContenu() == null || dto.getContenu().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu du post est obligatoire");
    }

    private void validateUser(String userId) {
        signupRepository.findByEmail(userId)
                .or(() -> signupRepository.findById(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private String resolveExistingUserKey(String userId) {
        return signupRepository.findByEmail(userId)
                .or(() -> signupRepository.findById(userId))
                .map(user -> user.getEmail() != null ? user.getEmail() : user.getId())
                .orElse(userId);
    }

    private int safePage(int page) { return Math.max(page, 0); }
    private int safeSize(int size) { return Math.min(Math.max(size, 1), 100); }
}
