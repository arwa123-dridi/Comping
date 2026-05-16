package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Set<String> specializations;
    private List<String> activeIncidentIds;
    private List<String> activeAlertIds;
    private Integer maxConcurrentIncidents;
    private Integer maxConcurrentAlerts;
    private Boolean available;
    private String role;
    private String team;
    private Double averageResolutionTime;
    private Integer totalIncidentsResolved;
    private Integer totalAlertsResolved;
}
