package tn.comping.spring.backendcomping.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private String idIncident;
    private String type;
    private String statut;
    private String descrition;
    private Date dateDeclaration;
    private boolean resolu;
    
    // Enhanced fields
    private String priorite;
    private String assigneId;
    private String categorie;
    private Integer estimatedResolutionMinutes;
    private Date dateResolution;
    private String resolution;
    private Integer impactScore;
    private List<String> tags;
    private String location;
    private String reporterId;}