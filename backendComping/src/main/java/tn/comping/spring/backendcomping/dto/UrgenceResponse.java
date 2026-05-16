package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class UrgenceResponse {
    private String id;
    private String titre;
    private String description;
    private Date dateCreation;
    private Date dateExpiration;
    private String statut;
    private String siteCampingId;
    private String userId;
    
    private String niveauUrgence;
    private Integer estimatedMinutesBeforeResolution;
    
    private String assigneId;
    private Date dateAssignment;
    private String resolution;
    private Date dateResolution;
    
    private Integer impactScore;
    private Double estimatedCost;
    private List<String> affectedUsers;
    
    private String categorie;
    private String priorite;
    private String reporterId;
    private List<String> tags;
    
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String location;
    private Double latitude;
    private Double longitude;
    
    private Date dateModification;
    private String modifiedBy;
    private Integer numberOfEscalations;
    
    private String notes;
    private List<String> comments;
}
