package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;

import java.util.List;

public interface CommentaireService {
    CommentaireResponseDTO createComment(CommentaireRequestDTO dto, String userId);
    List<CommentaireResponseDTO> getCommentairesByPost(String postId);
    CommentaireResponseDTO getCommentById(String id);
    CommentaireResponseDTO replyToComment(String commentId, CommentaireRequestDTO dto, String userId);
}
