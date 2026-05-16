package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.StatutEvent;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {

    private String idEvent;
    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private StatutEvent statut;
    private List<ActivityResponse> activities;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private int nombreParticipants;
    private int placesRestantes;
    private boolean dejaInscrit;
    private String categorie;
    private LocalDateTime createdAt;
    private String organisateurId;

    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double latitude;
    private double longitude;
    private String saison;
    private int dureeEnHeures;
<<<<<<< HEAD
    private List<String> participantIds;
=======
>>>>>>> origin/ahmed
}
