package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class SecuriteResponse {
    private String id;
    private String titre;
    private String description;
    private Date dateCreation;
    private Date dateDebut;
    private Date dateFin;
    private String statut;
    private String siteCampingId;
    
    private String typeMesure;
    private String niveauSecurite;
    private String zoneSecurisee;
    
    private String responsableId;
    private List<String> teamMemberIds;
    private Date dateAssignment;
    
    private List<String> equipmentUsed;
    private List<String> resourcesNeeded;
    private Double budgetAlloue;
    private Double budgetUtilise;
    
    private List<String> relatedIncidentIds;
    private List<String> relatedAlerteIds;
    private Integer numberOfIncidentsDetected;
    
    private Boolean conformiteAudit;
    private String certificateNumber;
    private Date certificateExpiry;
    private String complianceStatus;
    
    private String monitoringType;
    private List<String> monitoringLocations;
    private Date lastMonitoringDate;
    private String monitoringStatus;
    
    private Integer securityScore;
    private Integer riskScore;
    private String riskLevel;
    
    private List<String> findings;
    private List<String> recommendations;
    private String actionTaken;
    private Date dateActionTaken;
    
    private String reportedBy;
    private String approvedBy;
    private String notes;
    private Date dateModification;
}
