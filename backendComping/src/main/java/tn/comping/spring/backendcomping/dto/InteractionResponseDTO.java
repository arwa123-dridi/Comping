package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.CibleType;
import tn.comping.spring.backendcomping.entities.TypeInteraction;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InteractionResponseDTO {
    
    private String id;
    private TypeInteraction type;           // LIKE ou COMMENTAIRE
    private String utilisateurId;
    private String utilisateurEmail;
    private String utilisateurNom;           // Nom affiché
    
    private CibleType cibleType;
    private String cibleId;
    
    private String contenu;                 // Commentaire si existe
    private Date dateInteraction;
    private boolean visible;
}

