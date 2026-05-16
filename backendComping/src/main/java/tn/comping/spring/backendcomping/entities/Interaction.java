package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Interaction {
    @Id
    private String id;

    private String auteurId;
    private String type; // LIKE, REACTION, COMMENTAIRE

    private String cibleType; // AVIS, POST
    private String cibleId;

    private String contenu;  // For COMMENT only

    // Pour les réactions emoji (type = REACTION)
    private String emoji; // "❤️", "🔥", "👍", "😮", "😂"

    private Date dateCreation;

    // Unique constraint: (auteurId, cibleType, cibleId) pour éviter doublon LIKE/REACTION
}
