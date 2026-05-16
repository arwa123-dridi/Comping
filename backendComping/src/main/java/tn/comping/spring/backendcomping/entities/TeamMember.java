package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "TeamMember")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {
    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    
    @Builder.Default
    private Set<String> specializations = new HashSet<>(); // e.g., SECURITY, ELECTRICAL, PLUMBING, MEDICAL
    
    @Builder.Default
    private List<String> activeIncidentIds = new java.util.ArrayList<>();
    
    @Builder.Default
    private List<String> activeAlertIds = new java.util.ArrayList<>();
    
    private Integer maxConcurrentIncidents;
    private Integer maxConcurrentAlerts;
    
    private Boolean available;
    private LocalDateTime lastAvailableTime;
    private String role; // TECHNICIAN, SUPERVISOR, MANAGER
    private String team; // Team name
    
    private Double averageResolutionTime; // in minutes
    private Integer totalIncidentsResolved;
    private Integer totalAlertsResolved;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
}
