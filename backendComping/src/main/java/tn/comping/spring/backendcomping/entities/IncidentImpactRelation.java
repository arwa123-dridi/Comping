package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "IncidentImpactRelation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentImpactRelation {
    @Id
    private String id;
    
    private String causeIncidentId;
    private String affectedIncidentId;
    
    // Causality strength: 0-1
    private Double impactStrength;
    
    // Predicted delay (minutes) between cause and effect
    private Integer delayMinutes;
    
    private String relationshipType; // DIRECT, INDIRECT, CASCADING
    
    // Description of relationship
    private String description;
    
    private Integer occurrenceCount;
    private LocalDateTime detectedAt;
    private Boolean confirmed;
}
