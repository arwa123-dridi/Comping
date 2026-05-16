package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Document(collection = "Incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //pour un constructeur par defaut
@ToString
public class Incident {

    @Id
    private String idIncident;
    private String type;
    private String statut;
    private String descrition;
    private Date dateDeclaration;
    private boolean resolu;
    
    // Enhanced fields for advanced workflows
    private String priorite; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private String assigneId; // ID de la personne assignée
    private String categorie; // MAINTENANCE, SECURITE, PERSONNEL, AUTRE
    private Integer estimatedResolutionMinutes;
    private Date dateResolution;
    private String resolution; // Description de la résolution
    private Integer impactScore; // Score d'impact de 1-10
    private List<String> tags;
    private String location; // Localisation géographique
    private String reporterId; // ID de la personne qui a rapporté l'incident
    
}
