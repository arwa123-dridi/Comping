package tn.comping.spring.backendcomping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tn.comping.spring.backendcomping.entities.CibleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InteractionRequestDTO {
    
    @NotNull(message = "Type d'interaction obligatoire")
    private CibleType cibleType;        // AVIS ou POST
    
    @NotBlank(message = "ID cible obligatoire")
    private String cibleId;             // ID de l'avis/post
    
    // Optionnel pour LIKE, obligatoire pour COMMENTAIRE
    private String contenu;             // Texte du commentaire (null pour LIKE)
}

