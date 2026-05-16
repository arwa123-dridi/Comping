package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.TeamMemberResponse;
import tn.comping.spring.backendcomping.entities.TeamMember;

@Component
public class TeamMemberMapper {
    
    public TeamMemberResponse toResponse(TeamMember entity) {
        if (entity == null) return null;
        
        return TeamMemberResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .specializations(entity.getSpecializations())
                .activeIncidentIds(entity.getActiveIncidentIds())
                .activeAlertIds(entity.getActiveAlertIds())
                .maxConcurrentIncidents(entity.getMaxConcurrentIncidents())
                .maxConcurrentAlerts(entity.getMaxConcurrentAlerts())
                .available(entity.getAvailable())
                .role(entity.getRole())
                .team(entity.getTeam())
                .averageResolutionTime(entity.getAverageResolutionTime())
                .totalIncidentsResolved(entity.getTotalIncidentsResolved())
                .totalAlertsResolved(entity.getTotalAlertsResolved())
                .build();
    }
}
