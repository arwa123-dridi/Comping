package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class CommentaireResponseDTO {
    private String id;
    private String postId;
    private String parentCommentId;
    private String auteurId;
    private String auteurNom;
    private String contenu;
    private Date datePublication;
    private int likesCount;
    private boolean likedByCurrentUser;
    private int niveau;
    private List<CommentaireResponseDTO> replies;
}
