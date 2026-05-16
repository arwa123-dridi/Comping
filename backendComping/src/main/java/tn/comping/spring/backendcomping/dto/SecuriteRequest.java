package tn.comping.spring.backendcomping.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class SecuriteRequest {
    
    @NotBlank(message = "Titre est requis")
    @Size(min = 3, max = 100, message = "Titre doit être entre 3 et 100 caractères")
    private String titre;
    
    @NotBlank(message = "Description est requise")
    @Size(min = 10, max = 1000, message = "Description doit être entre 10 et 1000 caractères")
    private String description;
    
    @NotNull(message = "Site camping ID est requis")
    private String siteCampingId;
    
    @NotBlank(message = "Type de mesure est requis")
    @Pattern(regexp = "^(SURVEILLANCE|CONTROLE_ACCES|PATROUILLE|INSPECTION|AUTRE)$", 
             message = "Type de mesure invalide")
    private String typeMesure;
    
    @NotBlank(message = "Niveau de sécurité est requis")
    @Pattern(regexp = "^(BASSE|MOYENNE|HAUTE|CRITIQUE)$", message = "Niveau invalide")
    private String niveauSecurite;
    
    @NotBlank(message = "Zone sécurisée est requise")
    private String zoneSecurisee;
    
    @NotBlank(message = "Responsable ID est requis")
    private String responsableId;
    
    @NotBlank(message = "Type de surveillance est requis")
    @Pattern(regexp = "^(CCTV|PERSONNEL|SENSOR|MANUAL|AUTRE)$", 
             message = "Type de surveillance invalide")
    private String monitoringType;
    
    @Min(value = 1, message = "Score de sécurité minimum 1")
    @Max(value = 10, message = "Score de sécurité maximum 10")
    private Integer securityScore;
    
    @Min(value = 1, message = "Score de risque minimum 1")
    @Max(value = 10, message = "Score de risque maximum 10")
    private Integer riskScore;
    
    @DecimalMin(value = "0.0", message = "Budget alloué doit être positif")
    @DecimalMax(value = "999999.99")
    private Double budgetAlloue;
    
    private List<String> equipmentUsed;
    private List<String> monitoringLocations;
    private String notes;
}
