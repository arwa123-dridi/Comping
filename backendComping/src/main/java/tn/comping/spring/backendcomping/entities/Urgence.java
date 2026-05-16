package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "Urgence")
public class Urgence {
    @Id
    private String id;
    private String titre;
    private String description;
    private Date dateCreation;
    private Date dateExpiration;
    private String statut; // ATTENDANT, ACCEPTE, REJETEE, COMPLETEE
    private String siteCampingId;
    private String userId;
    
    // Priority & Urgency levels
    private String niveauUrgence; // IMMEDIATE, TRES_URGENT, URGENT, NORMAL, BASSE
    private Integer estimatedMinutesBeforeResolution;
    
    // Assignment & Workflow
    private String assigneId;
    private Date dateAssignment;
    private String resolution;
    private Date dateResolution;
    
    // Impact & Cost
    private Integer impactScore; // 1-10
    private Double estimatedCost;
    private List<String> affectedUsers;
    
    // Additional Metadata
    private String categorie; // MAINTENANCE, MEDICAL, SECURITE, PERSONNEL, AUTRE
    private String priorite; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private String reporterId;
    private List<String> tags;
    private List<String> attachments;
    
    // Contact & Location
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String location;
    private Double latitude;
    private Double longitude;
    
    // Audit Trail
    private Date dateModification;
    private String modifiedBy;
    private Integer numberOfEscalations;
    private List<String> escalationHistory;
    
    // Notes
    private String notes;
    private List<String> comments;
}
