package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "reponses_avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReponseAvis {

    @Id
    private String id;

    private String contenu;
    private Date dateReponse;

    private String avisId;
    private String auteurId;
    private String roleAuteur;
}