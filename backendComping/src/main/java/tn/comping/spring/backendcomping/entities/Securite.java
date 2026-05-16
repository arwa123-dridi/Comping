package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "Securite")
public class Securite {
    @Id
    private String id;
    private String titre;
    private String description;
    private Date dateCreation;
    private Date dateDebut;
    private Date dateFin;
    private String statut; // PLANIFIEE, EN_COURS, COMPLETEE, ANNULEE
    private String siteCampingId;
    
    // Security Details
    private String typeMesure; // SURVEILLANCE, CONTROLE_ACCES, PATROUILLE, INSPECTION, AUTRE
    private String niveauSecurite; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private String zoneSecurisee;
    
    // Assignment & Team
    private String responsableId;
    private List<String> teamMemberIds;
    private Date dateAssignment;
    
    // Resources
    private List<String> equipmentUsed;
    private List<String> resourcesNeeded;
    private Double budgetAlloue;
    private Double budgetUtilise;
    
    // Incidents & Issues
    private List<String> relatedIncidentIds;
    private List<String> relatedAlerteIds;
    private Integer numberOfIncidentsDetected;
    
    // Compliance & Documentation
    private Boolean conformiteAudit;
    private String certificateNumber;
    private Date certificateExpiry;
    private String complianceStatus;
    private List<String> attachments;
    
    // Monitoring
    private String monitoringType; // CCTV, PERSONNEL, SENSOR, MANUAL, AUTRE
    private List<String> monitoringLocations;
    private Date lastMonitoringDate;
    private String monitoringStatus; // ACTIF, INACTIF, MAINTENANCE
    
    // Assessment & Score
    private Integer securityScore; // 1-10
    private Integer riskScore; // 1-10
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    // Findings & Recommendations
    private List<String> findings;
    private List<String> recommendations;
    private String actionTaken;
    private Date dateActionTaken;
    
    // Metadata
    private String inspectionType;
    private String reportedBy;
    private String approvedBy;
    private String notes;
    private List<String> comments;
    private Date dateModification;
}
