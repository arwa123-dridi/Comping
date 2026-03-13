package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Avis {

    @Id
    private String id;

    private int note;
    private String commentaire;
    private Date datePublication;
    private StatutAvis statut;
    private boolean valide;

    private String utilisateurId;
    private String cibleId;
    private TypeCible typeCible;

    private Date dateModification;
    private String moderateurId;
    private String motifRejet;
}
