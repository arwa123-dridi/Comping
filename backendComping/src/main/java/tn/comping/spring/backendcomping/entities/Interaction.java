package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String type; // LIKE, COMMENTAIRE
    
    private String cibleType; // AVIS, POST
    private String cibleId;
    
    private String contenu; // For COMMENT only
    
    // Unique constraint: (auteurId, cibleType, cibleId, type=LIKE)
}
