package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import tn.comping.spring.backendcomping.enums.Difficulte;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class SortieRequestDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit faire entre 3 et 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    private String lieuDepart;

    private String region;

    @NotNull(message = "La difficulté est obligatoire")
    private Difficulte difficulte;

    @Min(value = 1, message = "Capacité minimum: 1")
    @Max(value = 50, message = "Capacité maximum: 50")
    private Integer capaciteMax;

    @Min(value = 0, message = "Le prix doit être positif")
    private Double prixParPersonne;

    private String equipementRequis;
    private Boolean assistanceMedicale;

    // IDs simples
    private String organisateurId;
    private String organisateurNom;
    private String equipeId;
}