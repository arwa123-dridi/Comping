package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReponseAvisDTO {

    private String id;
    private String contenu;
    private Date dateReponse;
    private String auteurId;
    private String auteurNom;

}