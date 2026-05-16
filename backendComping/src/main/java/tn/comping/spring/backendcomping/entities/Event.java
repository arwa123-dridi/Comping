package tn.comping.spring.backendcomping.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import lombok.*;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Event {
    @Id
    private String idEvent;
    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private StatutEvent statut;
    
    private String lieu;
    private String categorie;
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private int dureeEnHeures;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    @Builder.Default
    private ArrayList<Object> participantIds = new ArrayList<>();
}

