package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSuggestionResponse {
    private String incidentOrAlertId;
    private Boolean hasSuggestion;
    private String suggestedTeamMemberId;
    private String suggestedTeamMemberName;
    private String reason;
    private Double confidenceScore;
}
