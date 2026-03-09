package tn.comping.spring.backendcomping.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class Event {

    @Id
    private String idEvent;

    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private StatutEvent statut;

}
