package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "Avis")
public class Avis {
    @Id
    private String id;
    private String siteCampingId;
    private String utilisateurId;
    private int note; // 1 à 5
    private String commentaire;
    private Date dateCreation;
    private String itineraire;
    private String convention;
    private String statutModeration; // PUBLIE, SIGNALE, SUPPRIME
}