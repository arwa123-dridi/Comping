package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;
import jakarta.validation.constraints.*;
import java.util.List;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AlerteRequest {
    @NotBlank(message = "ID site camping est requis")
    @Size(min = 3, max = 50, message = "ID site doit être entre 3 et 50 caractères")
    private String siteCampingId;
    
    @NotNull(message = "Type d'alerte est requis")
    private TypeAlerte type;
    
    @NotBlank(message = "Titre est requis")
    @Size(min = 3, max = 100, message = "Titre doit être entre 3 et 100 caractères")
    private String titre;
    
    @NotBlank(message = "Description est requise")
    @Size(min = 10, max = 1000, message = "Description doit être entre 10 et 1000 caractères")
    private String description;
    
    @NotBlank(message = "Position est requise")
    @Pattern(regexp = "^(-?\\d+\\.\\d+),\\s*(-?\\d+\\.\\d+)$", message = "Position doit être format: lat, long")
    private String position;
    
    // Enhanced fields with validation
    @Pattern(regexp = "^(BASSE|MOYENNE|HAUTE|CRITIQUE)$", message = "Priorité invalide")
    private String priorite;
    
    @Size(max = 50, message = "ID assigné ne peut pas dépasser 50 caractères")
    private String assigneId;
    
    @Size(max = 50, message = "ID reporter ne peut pas dépasser 50 caractères")
    private String reporterId;
    
    @Size(max = 20, message = "Maximum 20 utilisateurs affectés")
    private List<String> affectedUsers;
    
    @Size(max = 100, message = "Équipement ne peut pas dépasser 100 caractères")
    private String equipmentAffected;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Coût estimé doit être positif")
    @DecimalMax(value = "999999.99", message = "Coût estimé est trop élevé")
    private Double estimatedCost;
    
    @Size(max = 5, message = "Maximum 5 fichiers attachés")
    private List<String> attachments;
}
