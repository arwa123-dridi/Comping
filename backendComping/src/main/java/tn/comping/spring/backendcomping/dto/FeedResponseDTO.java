package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FeedResponseDTO {
    
    private String id;
    private String contenu;
    private List<String> images;
    
    private String auteurId;
    private String auteurNom;
    
    private Date datePublication;
    
    // Métriques sociales
    private long likes;
    private List<String> commentairesPreviews;
    
    private boolean jAiLike;
    private boolean estPartageAvis;
}

