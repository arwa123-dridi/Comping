package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class EquipeRequestDTO {

    @NotBlank(message = "Le nom de l'équipe est obligatoire")
    @Size(min = 3, max = 50)
    private String nom;

    private String description;

    @Min(2)
    @Max(20)
    private Integer nbMembresMax;

    private String niveau;

    // doit être obligatoire
    @NotBlank(message = "Organisateur obligatoire")
    private String organisateurId;

    private String organisateurNom;
}