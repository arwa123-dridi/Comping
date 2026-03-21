package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class EquipeRequestDTO {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom doit faire entre 3 et 50 caractères")
    private String nom;

    private String description;

    @Min(value = 2, message = "Une équipe doit avoir au moins 2 membres")
    @Max(value = 20, message = "Capacité maximum: 20")
    private Integer nbMembresMax;
    private String niveau;

    // Organisateur (celui qui crée l'équipe)
    @NotBlank(message = "Organisateur obligatoire")
    private String organisateurId;
    private String organisateurNom;
}