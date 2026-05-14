package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAiDTO {
    private String idEvent;
    private String titre;
    private String description;
    private double prix;
    private String lieu;
    private String categorie;
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private int dureeEnHeures;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
