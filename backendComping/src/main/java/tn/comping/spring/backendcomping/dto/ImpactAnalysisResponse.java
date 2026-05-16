package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAnalysisResponse {
    private String incidentId;
    private String incidentType;
    private Set<String> directlyAffected;
    private Integer totalPotentialImpact;
    private Double averageImpactStrength;
}
