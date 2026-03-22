package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypePost;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PostResponseDTO {
    
    private String id;
    private String contenu;
    private List<String> images;
    
    private TypePost typePost;
    private String utilisateurId;
    private String utilisateurNom;       // Nom complet utilisateur
    private String utilisateurEmail;
    
    private String avisId;              // Si partage avis
    
    private boolean visible;
    private Date dateCreation;
    private Date dateModification;
    
    // Métadonnées sociales (PHASE 1 intégrée)
    private long nombreLikes;
    private List<String> derniersCommentaires;  // Aperçu
}

