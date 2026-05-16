package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;



@Document(collection = "CreneauLivraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class CreneauLivraison {

    @Id
    private String idCreneauLivraison;
    private LocalTime heureFin;
    private LocalTime  heureDebut;
    private boolean disponible;

}
