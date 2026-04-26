package tn.comping.spring.backendcomping.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutEvent statut;
    private String lieu;
    private String organisateurId;
    private List<String> participantIds;
    private String imageUrl;
    private String categorie;
    private LocalDateTime createdAt;
    private List<String> activityIds;

    // ✅ AJOUTS POUR L'IA
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double latitude;
    private double longitude;
    private String saison;
    private int dureeEnHeures;

}
