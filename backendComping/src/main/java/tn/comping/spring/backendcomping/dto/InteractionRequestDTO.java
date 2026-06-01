package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionRequestDTO {
    private String type; // LIKE, COMMENTAIRE
    private String cibleType; // AVIS, POST
    private String cibleId;
    private String contenu; // For COMMENT
}
