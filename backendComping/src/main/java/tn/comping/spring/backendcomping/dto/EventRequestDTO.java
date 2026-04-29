package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.entities.StatutEvent;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestDTO {

    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private StatutEvent statut;
    private List<String> activityIds;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private String categorie;

    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double latitude;
    private double longitude;
    private String saison;
    private int dureeEnHeures;


}
