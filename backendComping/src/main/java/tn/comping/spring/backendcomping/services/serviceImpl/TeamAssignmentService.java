package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.AssignmentSuggestionResponse;
import tn.comping.spring.backendcomping.dto.TeamMemberResponse;
import java.util.List;

public interface TeamAssignmentService {
    AssignmentSuggestionResponse suggestAssignmentForIncident(String incidentId);
    AssignmentSuggestionResponse suggestAssignmentForAlert(String alertId);
    TeamMemberResponse assignIncident(String incidentId, String teamMemberId);
    TeamMemberResponse assignAlert(String alertId, String teamMemberId);
    void unassignIncident(String incidentId);
    void unassignAlert(String alertId);
    List<TeamMemberResponse> getAvailableTeamMembers();
    List<TeamMemberResponse> getTeamMembersBySpecialization(String specialization);
}
