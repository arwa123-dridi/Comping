package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "EscalationRule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscalationRule {
    @Id
    private String id;
    private String name;
    private String description;
    private Boolean enabled;
    
    // Trigger conditions
    private String sourceType; // INCIDENT, ALERT, or BOTH
    private String triggerPriority; // BASSE, MOYENNE, HAUTE, CRITIQUE
    private Integer minutesBeforeEscalation; // Time before escalate
    private String triggerCategory; // Optional category filter
    
    // Escalation action
    private String escalateToRole; // MANAGER, ADMIN, SUPERVISOR
    private String escalateToUserId; // Specific user (if not role-based)
    private String notificationMethod; // EMAIL, SMS, BOTH
    private String escalationMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
}
