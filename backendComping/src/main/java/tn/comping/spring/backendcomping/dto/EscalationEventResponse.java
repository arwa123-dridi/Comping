package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationEventResponse {
    private String id;
    private String incidentOrAlertId;
    private String sourceType;
    private String fromLevel;
    private String toLevel;
    private LocalDateTime escalationTime;
    private String reason;
    private String escalatedToUserId;
    private String escalatedToRole;
    private LocalDateTime notificationSentTime;
    private Boolean notificationAcknowledged;
}
