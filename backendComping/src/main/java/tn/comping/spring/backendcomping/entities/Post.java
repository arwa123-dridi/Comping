package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post {
    @Id
    private String id;

    private String auteurId;
    private String typePost; // FEED, STORY, AVIS_LIE

    private String avisId;       // Optionnel : lien vers un Avis
    private String cibleType;    // POST, AVIS
    private String cibleId;

    private String contenu;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private Date datePublication;

    @Builder.Default
    private int likesCount = 0;
    @Builder.Default
    private int commentairesCount = 0;

    // === REACTIONS (emoji) - ex: {"❤️":5, "🔥":3, "👍":12} ===
    @Builder.Default
    private Map<String, Integer> reactions = new HashMap<>();

    // Hashtags extraits automatiquement du contenu
    @Builder.Default
    private List<String> hashtags = new ArrayList<>();

    // Score IA tendance (calculé périodiquement)
    @Builder.Default
    private double trendScore = 0.0;

    // Visibilité
    @Builder.Default
    private String visibilite = "PUBLIC"; // PUBLIC, AMIS, PRIVE
}
