package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteractionResponseDTO {
    private String id;
    private String auteurNom;
    private String type;
    private String cibleType;
    private String cibleId;
    private String contenu;
}
