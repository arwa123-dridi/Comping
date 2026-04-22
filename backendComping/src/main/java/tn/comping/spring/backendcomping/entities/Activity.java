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

    // ✅ AJOUTS POUR L'IA
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double prix;
    private String lieu;
    private double latitude;
    private double longitude;
    private String saison;
    private String meteoRecommandee;


}
