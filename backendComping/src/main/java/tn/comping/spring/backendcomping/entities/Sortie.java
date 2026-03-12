package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import tn.comping.spring.backendcomping.enums.Difficulte;
import tn.comping.spring.backendcomping.enums.StatutSortie;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "sorties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sortie {
    @Id
    private String id;

    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieuDepart;
    private String lieuArrivee;
    private String region;
    private Difficulte difficulte;
    private Double distanceKm;
    private Integer capaciteMax;
    private Double prixParPersonne;
    private String equipementRequis;
    private Boolean assistanceMedicale;
    private StatutSortie statut;

    // IDs simples de l'organisateur
    private String organisateurId;
    private String organisateurNom;

    // Relation avec Equipe
    private String equipeId;
    private String equipeNom;

    // Liste des IDs des participants
    @Builder.Default
    private List<String> participantIds = new ArrayList<>();
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}