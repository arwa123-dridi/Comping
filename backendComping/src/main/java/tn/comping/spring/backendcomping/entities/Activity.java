package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class Activity {

    @Id
    private String idActivity;
    private String nom;
    private String description;
    private String type;
    private String duree ;
    private String capacite;


    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private List<String> tags;


}
