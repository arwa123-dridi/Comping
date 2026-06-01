package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDTO {
    private String id;
    private String auteurId;
    private String auteurNom;
    private String auteurPhoto;
    private String typePost;
    private String avisId;
    private String cibleType;
    private String cibleId;
    private String contenu;
    private List<String> images;
    private Date datePublication;
    private int likesCount;
    private int commentairesCount;
    private boolean likedByCurrentUser;

    // Reactions emoji {"❤️":5, "🔥":3}
    private Map<String, Integer> reactions;
    private String myReaction; // la réaction de l'utilisateur courant

    // IA - score tendance + hashtags
    private List<String> hashtags;
    private double trendScore;

    private String visibilite;
}
