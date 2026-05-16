package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;
import java.util.Date;
import java.util.List;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
public class AlerteResponse {
    private String id;
    private String siteCampingId;
    private TypeAlerte type;
    private String titre;
    private String description;
    private Date dateDeclenchement;
    private String statut;
    private String position;
    
    // Enhanced fields
    private String priorite;
    private String assigneId;
    private Date dateResolution;
    private String resolution;
    private Integer responseTimeMinutes;
    private String reporterId;
    private List<String> affectedUsers;
    private String equipmentAffected;
    private Double estimatedCost;
    private List<String> attachments;
    private String escalationNotes;
}