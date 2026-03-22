package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post {

    @Id
    private String id;

    private String contenu;                 // Texte principal (obligatoire pour TEXTE)
    private List<String> images;            // URLs images (optionnel)

    private TypePost typePost;              // TEXTE, IMAGE, PARTAGE_AVIS
    private String utilisateurId;           // Auteur post
    private String utilisateurEmail;        // Cache email

    private String avisId;                  // Si PARTAGE_AVIS (lien avis)
    
    @Builder.Default
    private boolean visible = true;         // Soft delete / modération
    private Date dateCreation;
    private Date dateModification;
    
    // Constructeur création
    public Post(String utilisateurId, String utilisateurEmail, String contenu, List<String> images, 
                TypePost typePost, String avisId) {
        this.utilisateurId = utilisateurId;
        this.utilisateurEmail = utilisateurEmail;
        this.contenu = contenu;
        this.images = images;
        this.typePost = typePost;
        this.avisId = avisId;
        this.dateCreation = new Date();
        this.dateModification = new Date();
    }
}
