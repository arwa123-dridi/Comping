package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.CommentaireRequestDTO;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;

import java.util.List;

public interface CommentaireService {
    CommentaireResponseDTO createComment(CommentaireRequestDTO dto, String userId);
    List<CommentaireResponseDTO> getCommentairesByPost(String postId, String userId);
    CommentaireResponseDTO getCommentById(String id);
    CommentaireResponseDTO replyToComment(String commentId, CommentaireRequestDTO dto, String userId);
    CommentaireResponseDTO updateComment(String commentId, String contenu, String userId);
    void deleteComment(String commentId, String userId);
    void likeComment(String commentId, String userId);
    void unlikeComment(String commentId, String userId);
}
