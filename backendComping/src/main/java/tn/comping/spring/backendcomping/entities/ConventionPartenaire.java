package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "ConventionPartenaire")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class ConventionPartenaire {

    @Id
    private String idConventionPartenaire;
    private Date dateDebut;
    private Date dateFin;
    private double remise;
    private String conditions;

}
