package tn.comping.spring.backendcomping.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class UrgenceRequest {
    
    @NotBlank(message = "Titre est requis")
    @Size(min = 3, max = 100, message = "Titre doit être entre 3 et 100 caractères")
    private String titre;
    
    @NotBlank(message = "Description est requise")
    @Size(min = 10, max = 1000, message = "Description doit être entre 10 et 1000 caractères")
    private String description;
    
    @NotNull(message = "Site camping ID est requis")
    private String siteCampingId;
    
    @NotNull(message = "User ID est requis")
    private String userId;
    
    @NotBlank(message = "Niveau d'urgence est requis")
    @Pattern(regexp = "^(IMMEDIATE|TRES_URGENT|URGENT|NORMAL|BASSE)$", 
             message = "Niveau d'urgence invalide")
    private String niveauUrgence;
    
    @NotNull(message = "Minutes avant résolution est requis")
    @Min(value = 1, message = "Minimum 1 minute")
    @Max(value = 1440, message = "Maximum 24 heures (1440 minutes)")
    private Integer estimatedMinutesBeforeResolution;
    
    @NotBlank(message = "Catégorie est requise")
    @Pattern(regexp = "^(MAINTENANCE|MEDICAL|SECURITE|PERSONNEL|AUTRE)$", 
             message = "Catégorie invalide")
    private String categorie;
    
    @NotBlank(message = "Priorité est requise")
    @Pattern(regexp = "^(BASSE|MOYENNE|HAUTE|CRITIQUE)$", message = "Priorité invalide")
    private String priorite;
    
    @NotBlank(message = "Reporter ID est requis")
    private String reporterId;
    
    @Min(value = 1, message = "Impact score minimum 1")
    @Max(value = 10, message = "Impact score maximum 10")
    private Integer impactScore;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Coût estimé doit être positif")
    @DecimalMax(value = "999999.99")
    private Double estimatedCost;
    
    @NotBlank(message = "Nom du contact est requis")
    private String contactName;
    
    @NotBlank(message = "Téléphone du contact est requis")
    @Pattern(regexp = "^[+]?[0-9]{10,}$", message = "Téléphone invalide")
    private String contactPhone;
    
    @Email(message = "Email invalide")
    private String contactEmail;
    
    @Pattern(regexp = "^(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)$|^$", 
             message = "Position doit être format: lat, long ou vide")
    private String location;
    
    private List<String> tags;
    private String notes;
}
