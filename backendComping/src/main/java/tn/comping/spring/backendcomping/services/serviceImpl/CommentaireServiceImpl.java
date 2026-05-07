package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.entities.Commentaire;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.CommentaireService;
import tn.comping.spring.backendcomping.utils.mapper.CommentaireMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentaireServiceImpl implements CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final PostRepository postRepository;
    private final SignupRepository signupRepository;
    private final CommentaireMapper commentaireMapper;

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

        log.info("Commentaire cree - ID: {}, Post: {}", saved.getId(), dto.getPostId());
        return commentaireMapper.toResponseDTO(saved);
    }

    @Override
    public List<CommentaireResponseDTO> getCommentairesByPost(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouve");
        }

        return commentaireRepository.findByPostIdAndParentCommentIdOrderByDatePublicationAsc(postId, null)
                .stream()
                .map(this::buildCommentaireTree)
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

    private CommentaireResponseDTO buildCommentaireTree(Commentaire comment) {
        CommentaireResponseDTO dto = commentaireMapper.toResponseDTO(comment);

        List<CommentaireResponseDTO> replies = commentaireRepository.findByParentCommentIdOrderByDatePublicationAsc(comment.getId())
                .stream()
                .map(this::buildCommentaireTree)
                .collect(Collectors.toList());

        dto.setReplies(replies);
        return dto;
    }

    private void validatePayload(CommentaireRequestDTO dto) {
        if (dto == null || isBlank(dto.getPostId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le post est obligatoire");
        }
        if (isBlank(dto.getContenu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu du commentaire est obligatoire");
        }
    }

    private void validateUser(String userId) {
        signupRepository.findByEmail(userId)
                .or(() -> signupRepository.findById(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
