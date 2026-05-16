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
    private String lieuArrivee;
    private String region;
    private Difficulte difficulte;
    private Integer capaciteMax;
    private Integer placesDisponibles;
    private Double prixParPersonne;
    private StatutSortie statut;
    private String equipementRequis;
    private Boolean assistanceMedicale;
    private Double distanceKm;

    // ✅ AJOUTÉ — URL image Cloudinary
    private String imageUrl;

    private String organisateurId;
    private String organisateurNom;
    private String organisateurPrenom;
    private String equipeId;
    private String equipeNom;

    private Integer nombreParticipants;
    private List<String> participantIds;
    private LocalDateTime dateCreation;
<<<<<<< HEAD
    private String utilisateurPrenom;
}
=======
    public void setOrganisateurPrenom(String firstName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setOrganisateurPrenom'");
    }
}
>>>>>>> origin/ahmed
