package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentaireRequestDTO {
    private String postId;
    private String parentCommentId; // NULL pour commentaire direct sur post
    private String contenu;
}
