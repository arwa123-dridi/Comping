package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class CommentaireRequestDTO {
    private String postId;
    private String parentCommentId; // NULL pour commentaire direct sur post
    private String contenu;
    private List<String> mentionedIds; // emails des utilisateurs mentionnés (@)

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public List<String> getMentionedIds() { return mentionedIds; }
    public void setMentionedIds(List<String> mentionedIds) { this.mentionedIds = mentionedIds; }
}
