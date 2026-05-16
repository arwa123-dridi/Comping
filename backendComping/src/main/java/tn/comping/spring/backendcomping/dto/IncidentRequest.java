package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentRequest {
    @NotBlank(message = "Type d'incident est requis")
    @Size(min = 3, max = 50, message = "Type doit être entre 3 et 50 caractères")
    private String type;
    
    @NotBlank(message = "Statut est requis")
    @Pattern(regexp = "^(OUVERT|EN_COURS|RESOLU|FERME)$", message = "Statut invalide")
    private String statut;
    
    @NotBlank(message = "Description est requise")
    @Size(min = 10, max = 1000, message = "Description doit être entre 10 et 1000 caractères")
    private String descrition;
    
    @NotNull(message = "Date de déclaration est requise")
    @PastOrPresent(message = "Date ne peut pas être dans le futur")
    private Date dateDeclaration;
    
    private boolean resolu;
    
    // Enhanced fields with validation
    @Pattern(regexp = "^(BASSE|MOYENNE|HAUTE|CRITIQUE)$", message = "Priorité invalide")
    private String priorite;
    
    @Size(max = 50, message = "ID assigné ne peut pas dépasser 50 caractères")
    private String assigneId;
    
    @Pattern(regexp = "^(MAINTENANCE|SECURITE|PERSONNEL|AUTRE)$", message = "Catégorie invalide")
    private String categorie;
    
    @Min(value = 5, message = "Temps estimé minimum 5 minutes")
    @Max(value = 10080, message = "Temps estimé maximum 7 jours (10080 minutes)")
    private Integer estimatedResolutionMinutes;
    
    @Size(max = 500, message = "Résolution ne peut pas dépasser 500 caractères")
    private String resolution;
    
    @Min(value = 1, message = "Score d'impact minimum 1")
    @Max(value = 10, message = "Score d'impact maximum 10")
    private Integer impactScore;
    
    @Size(max = 10, message = "Maximum 10 tags")
    private List<String> tags;
    
    @Size(max = 100, message = "Location ne peut pas dépasser 100 caractères")
    private String location;
    
    @Size(max = 50, message = "ID reporter ne peut pas dépasser 50 caractères")
    private String reporterId;
}
