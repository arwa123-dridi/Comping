package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.entities.Commentaire;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.CommentaireService;
import tn.comping.spring.backendcomping.utils.mapper.CommentaireMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentaireServiceImpl implements CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final PostRepository postRepository;
    private final SignupRepository signupRepository;
    private final CommentaireMapper commentaireMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final InteractionRepository interactionRepository;

    @Override
    public CommentaireResponseDTO createComment(CommentaireRequestDTO dto, String userId) {
        validateUser(userId);
        validatePayload(dto);

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouve"));

        Commentaire parent = null;
        int niveau = 0;
        if (!isBlank(dto.getParentCommentId())) {
            parent = commentaireRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire parent non trouve"));
            if (!post.getId().equals(parent.getPostId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le commentaire parent n'appartient pas a ce post");
            }
            niveau = parent.getNiveau() + 1;
        }

        Commentaire commentaire = Commentaire.builder()
                .postId(dto.getPostId())
                .parentCommentId(parent != null ? parent.getId() : null)
                .auteurId(userId)
                .contenu(dto.getContenu().trim())
                .datePublication(new Date())
                .valide(true)
                .likesCount(0)
                .niveau(niveau)
                .build();

        Commentaire saved = commentaireRepository.save(commentaire);
        post.setCommentairesCount(post.getCommentairesCount() + 1);
        postRepository.save(post);

        String auteurNom = signupRepository.findByEmail(userId)
                .map(u -> (u.getFirstName() + " " + u.getLastName()).trim())
                .filter(n -> !n.isBlank())
                .orElse(userId);

        // Construire l'ensemble des personnes explicitement mentionnées (sauf l'auteur lui-même)
        Set<String> mentionedSet = new HashSet<>();
        if (dto.getMentionedIds() != null) {
            dto.getMentionedIds().stream()
                    .filter(id -> id != null && !id.equals(userId))
                    .forEach(mentionedSet::add);
        }

        // Notifications COMMENT / REPLY — seulement si la personne n'est PAS explicitement mentionnée
        // (elle recevra MENTION à la place, plus spécifique)
        if (parent == null) {
            if (!post.getAuteurId().equals(userId) && !mentionedSet.contains(post.getAuteurId())) {
                sendNotif(post.getAuteurId(), "COMMENT", auteurNom, saved.getId(), dto.getPostId());
            }
        } else {
            if (!parent.getAuteurId().equals(userId) && !mentionedSet.contains(parent.getAuteurId())) {
                sendNotif(parent.getAuteurId(), "REPLY", auteurNom, saved.getId(), dto.getPostId());
            }
            if (!post.getAuteurId().equals(userId)
                    && !post.getAuteurId().equals(parent.getAuteurId())
                    && !mentionedSet.contains(post.getAuteurId())) {
                sendNotif(post.getAuteurId(), "REPLY", auteurNom, saved.getId(), dto.getPostId());
            }
        }

        // Notifications MENTION — toujours envoyées pour chaque personne taguée
        for (String mentionedId : mentionedSet) {
            sendNotif(mentionedId, "MENTION", auteurNom, saved.getId(), dto.getPostId());
        }

        log.info("Commentaire cree - ID: {}, Post: {}", saved.getId(), dto.getPostId());
        return commentaireMapper.toResponseDTO(saved);
    }

    @Override
    public List<CommentaireResponseDTO> getCommentairesByPost(String postId, String userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouve");
        }

        return commentaireRepository.findByPostIdAndParentCommentIdOrderByDatePublicationAsc(postId, null)
                .stream()
                .map(c -> buildCommentaireTree(c, userId))
                .collect(Collectors.toList());
    }

    @Override
    public CommentaireResponseDTO getCommentById(String id) {
        Commentaire commentaire = commentaireRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire non trouve"));
        return commentaireMapper.toResponseDTO(commentaire);
    }

    @Override
    public CommentaireResponseDTO replyToComment(String commentId, CommentaireRequestDTO dto, String userId) {
        Commentaire parent = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire parent non trouve"));

        dto.setParentCommentId(commentId);
        dto.setPostId(parent.getPostId());

        return createComment(dto, userId);
    }

    private CommentaireResponseDTO buildCommentaireTree(Commentaire comment, String userId) {
        CommentaireResponseDTO dto = commentaireMapper.toResponseDTO(comment);

        if (userId != null) {
            boolean liked = interactionRepository
                    .findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "COMMENT", comment.getId(), "LIKE")
                    .isPresent();
            dto.setLikedByCurrentUser(liked);
        }

        List<CommentaireResponseDTO> replies = commentaireRepository
                .findByParentCommentIdOrderByDatePublicationAsc(comment.getId())
                .stream()
                .map(c -> buildCommentaireTree(c, userId))
                .collect(Collectors.toList());

        dto.setReplies(replies);
        return dto;
    }

    @Override
    public void likeComment(String commentId, String userId) {
        validateUser(userId);
        Commentaire comment = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire non trouvé"));
        boolean alreadyLiked = interactionRepository
                .findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "COMMENT", commentId, "LIKE")
                .isPresent();
        if (!alreadyLiked) {
            interactionRepository.save(Interaction.builder()
                    .auteurId(userId).cibleType("COMMENT").cibleId(commentId)
                    .type("LIKE").dateCreation(new Date()).build());
            comment.setLikesCount(comment.getLikesCount() + 1);
            commentaireRepository.save(comment);
        }
    }

    @Override
    public void unlikeComment(String commentId, String userId) {
        Commentaire comment = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire non trouvé"));
        interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(userId, "COMMENT", commentId, "LIKE")
                .ifPresent(like -> {
                    interactionRepository.delete(like);
                    comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
                    commentaireRepository.save(comment);
                });
    }

    private void validatePayload(CommentaireRequestDTO dto) {
        if (dto == null || isBlank(dto.getPostId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le post est obligatoire");
        }
        if (isBlank(dto.getContenu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu du commentaire est obligatoire");
        }
    }

    @Override
    public CommentaireResponseDTO updateComment(String commentId, String contenu, String userId) {
        Commentaire comment = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire non trouvé"));
        if (!comment.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        if (contenu == null || contenu.trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu est obligatoire");
        comment.setContenu(contenu.trim());
        return commentaireMapper.toResponseDTO(commentaireRepository.save(comment));
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        Commentaire comment = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commentaire non trouvé"));
        Post post = postRepository.findById(comment.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouvé"));
        if (!comment.getAuteurId().equals(userId) && !post.getAuteurId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");

        int deleted = deleteRecursively(commentId);
        post.setCommentairesCount(Math.max(0, post.getCommentairesCount() - deleted));
        postRepository.save(post);
        log.info("Commentaire supprimé - ID: {}, cascade: {} supprimés", commentId, deleted);
    }

    private int deleteRecursively(String commentId) {
        List<Commentaire> children = commentaireRepository.findByParentCommentIdOrderByDatePublicationAsc(commentId);
        int count = 1;
        for (Commentaire child : children) {
            count += deleteRecursively(child.getId());
        }
        commentaireRepository.deleteById(commentId);
        return count;
    }

    private void validateUser(String userId) {
        signupRepository.findByEmail(userId)
                .or(() -> signupRepository.findById(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private void sendNotif(String recipientEmail, String type, String auteurNom,
                           String commentId, String postId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("expediteurNom", auteurNom);
        payload.put("commentId", commentId);
        payload.put("postId", postId);
        messagingTemplate.convertAndSend("/topic/user/" + recipientEmail + "/notifications", payload);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
