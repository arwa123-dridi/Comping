package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeCible;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisRequestDTO {

    private int note;
    private String titre;
    private String contenu;
    private String cibleId;
    private TypeCible typeCible;
}