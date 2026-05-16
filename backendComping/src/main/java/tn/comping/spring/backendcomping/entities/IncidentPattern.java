package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "IncidentPattern")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPattern {
    @Id
    private String id;
    private String description;
    
    // e.g., ["PUMP_FAILURE", "HEATING_FAILURE"] triggers prediction of "WATER_OUTAGE"
    private java.util.List<String> triggerIncidentTypes;
    
    // What we predict will happen
    private String predictedIncidentType;
    
    // Confidence: 0-1
    private Double confidenceScore;
    
    // How often this pattern is observed
    private Integer occurrenceCount;
    
    // Average time between trigger and predicted incident
    private Integer timeToOccurMinutes;
    
    private LocalDateTime firstDetected;
    private LocalDateTime lastDetected;
    private Boolean enabled;
}
