package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import tn.comping.spring.backendcomping.entities.Difficulte;
import tn.comping.spring.backendcomping.entities.StatutSortie;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SortieResponseDTO {

    private String id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieuDepart;
    private String lieuArrivee;       // ville de destination
    private String region;
    private Difficulte difficulte;
    private Integer capaciteMax;
    private Integer placesDisponibles;
    private Double prixParPersonne;
    private StatutSortie statut;
    private String equipementRequis;
    private Boolean assistanceMedicale;
    private Double distanceKm;
    private String imageUrl;

    private String organisateurId;
    private String organisateurNom;
    private String organisateurPrenom;
    private String equipeId;
    private String equipeNom;

    private Integer nombreParticipants;
    private List<String> participantIds;
    private LocalDateTime dateCreation;
    private String utilisateurPrenom;

    // ✅ AJOUTÉ — checklist IA générée automatiquement à la création
    private String checklistRecommandee;
}