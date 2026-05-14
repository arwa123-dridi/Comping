package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireRequestDTO {
    private String postId;
    private String parentCommentId; // NULL pour commentaire direct sur post
    private String contenu;
    private List<String> mentionedIds; // emails des utilisateurs mentionnés (@)
}
