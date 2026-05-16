package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.EscalationEventResponse;
import tn.comping.spring.backendcomping.entities.EscalationEvent;

@Component
public class EscalationEventMapper {
    
    public EscalationEventResponse toResponse(EscalationEvent entity) {
        if (entity == null) return null;
        
        return EscalationEventResponse.builder()
                .id(entity.getId())
                .incidentOrAlertId(entity.getIncidentOrAlertId())
                .sourceType(entity.getSourceType())
                .fromLevel(entity.getFromLevel())
                .toLevel(entity.getToLevel())
                .escalationTime(entity.getEscalationTime())
                .reason(entity.getReason())
                .escalatedToUserId(entity.getEscalatedToUserId())
                .escalatedToRole(entity.getEscalatedToRole())
                .notificationSentTime(entity.getNotificationSentTime())
                .notificationAcknowledged(entity.getNotificationAcknowledged())
                .build();
    }
    
    public EscalationEvent toEntity(EscalationEventResponse dto) {
        if (dto == null) return null;
        
        return EscalationEvent.builder()
                .id(dto.getId())
                .incidentOrAlertId(dto.getIncidentOrAlertId())
                .sourceType(dto.getSourceType())
                .fromLevel(dto.getFromLevel())
                .toLevel(dto.getToLevel())
                .escalationTime(dto.getEscalationTime())
                .reason(dto.getReason())
                .escalatedToUserId(dto.getEscalatedToUserId())
                .escalatedToRole(dto.getEscalatedToRole())
                .notificationSentTime(dto.getNotificationSentTime())
                .notificationAcknowledged(dto.getNotificationAcknowledged())
                .build();
    }
}
