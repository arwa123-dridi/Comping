package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResponse {
    private String patternId;
    private String triggerIncidentType;
    private String predictedIncidentType;
    private Double confidenceScore;
    private Integer estimatedTimeToOccurMinutes;
    private Integer occurrenceHistory;
    private String recommendation;
}
