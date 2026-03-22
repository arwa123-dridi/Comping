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
    
    private TypeInteraction type;           // LIKE, COMMENTAIRE, PARTAGE
    private String utilisateurId;            // Auteur de l'interaction (JWT)
    private String utilisateurEmail;         // Cache email pour queries rapides
    
    private CibleType cibleType;            // AVIS, POST, COMMENTAIRE
    private String cibleId;                 // ID de l'avis/post/commentaire cible
    
    private String contenu;                 // Texte commentaire (null pour LIKE)
    @Builder.Default
    private boolean visible = true;         // Soft delete
    
    private Date dateInteraction;
    
    // Constructeur pour création
    public Interaction(String utilisateurId, String utilisateurEmail, TypeInteraction type, 
                      CibleType cibleType, String cibleId, String contenu) {
        this.utilisateurId = utilisateurId;
        this.utilisateurEmail = utilisateurEmail;
        this.type = type;
        this.cibleType = cibleType;
        this.cibleId = cibleId;
        this.contenu = contenu;
        this.dateInteraction = new Date();
    }
}
