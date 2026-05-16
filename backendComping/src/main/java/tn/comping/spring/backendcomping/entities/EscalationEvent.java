package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "EscalationEvent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationEvent {
    @Id
    private String id;
    private String incidentOrAlertId;
    private String sourceType; // INCIDENT or ALERT
    private String fromLevel; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private String toLevel;
    private LocalDateTime escalationTime;
    private String reason;
    private String escalatedToUserId;
    private String escalatedToRole; // MANAGER, ADMIN, etc.
    private LocalDateTime notificationSentTime;
    private Boolean notificationAcknowledged;
}
