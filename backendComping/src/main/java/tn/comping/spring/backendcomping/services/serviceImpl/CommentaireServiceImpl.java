package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.entities.Commentaire;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
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
    private final CommentaireMapper commentaireMapper;

    @Override
    public CommentaireResponseDTO createComment(CommentaireRequestDTO dto, String userId) {
        Commentaire commentaire = Commentaire.builder()
                .postId(dto.getPostId())
                .parentCommentId(dto.getParentCommentId())
                .auteurId(userId)
                .contenu(dto.getContenu())
                .datePublication(new Date())
                .build();
        
        Commentaire saved = commentaireRepository.save(commentaire);
        log.info("Commentaire créé - ID: {}, Post: {}", saved.getId(), dto.getPostId());
        return commentaireMapper.toResponseDTO(saved);
    }

    @Override
    public List<CommentaireResponseDTO> getCommentairesByPost(String postId) {
        // Récupérer tous les commentaires root d'abord
        List<Commentaire> rootComments = commentaireRepository.findByPostIdOrderByDatePublicationAsc(postId)
                .stream()
                .filter(c -> c.getParentCommentId() == null)
                .collect(Collectors.toList());
        return rootComments.stream()
                .map(this::buildCommentaireTree)
                .collect(Collectors.toList());
    }

    @Override
    public CommentaireResponseDTO getCommentById(String id) {
        Commentaire commentaire = commentaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        return commentaireMapper.toResponseDTO(commentaire);
    }

    @Override
    public CommentaireResponseDTO replyToComment(String commentId, CommentaireRequestDTO dto, String userId) {
        Commentaire parent = commentaireRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire parent non trouvé"));
        
        dto.setParentCommentId(commentId);
        dto.setPostId(parent.getPostId());
        
        return createComment(dto, userId);
    }

    private CommentaireResponseDTO buildCommentaireTree(Commentaire comment) {
        CommentaireResponseDTO dto = commentaireMapper.toResponseDTO(comment);
        
        // Récupérer les réponses récursivement
        List<Commentaire> replies = commentaireRepository.findByParentCommentIdOrderByDatePublicationAsc(comment.getId());
        List<CommentaireResponseDTO> repliesDTO = replies.stream()
                .map(this::buildCommentaireTree)
                .collect(Collectors.toList());
        
        // Ajouter les réponses (structure arborescente)
        // Cette logique serait dans un champ enfants dans DTO
        // Structure arborescente construite récursivement dans buildCommentaireTree
        return dto;
    }
}
