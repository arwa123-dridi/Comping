package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.AssignmentSuggestionResponse;
import tn.comping.spring.backendcomping.dto.TeamMemberResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.TeamAssignmentService;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TeamAssignmentController {

    private final TeamAssignmentService teamAssignmentService;

    @GetMapping("/suggest-incident/{incidentId}")
    public ResponseEntity<AssignmentSuggestionResponse> suggestAssignmentForIncident(
            @PathVariable String incidentId) {
        return ResponseEntity.ok(teamAssignmentService.suggestAssignmentForIncident(incidentId));
    }

    @GetMapping("/suggest-alert/{alertId}")
    public ResponseEntity<AssignmentSuggestionResponse> suggestAssignmentForAlert(
            @PathVariable String alertId) {
        return ResponseEntity.ok(teamAssignmentService.suggestAssignmentForAlert(alertId));
    }

    @PostMapping("/incident/{incidentId}/assign/{teamMemberId}")
    public ResponseEntity<TeamMemberResponse> assignIncident(
            @PathVariable String incidentId,
            @PathVariable String teamMemberId) {
        return ResponseEntity.ok(teamAssignmentService.assignIncident(incidentId, teamMemberId));
    }

    @PostMapping("/alert/{alertId}/assign/{teamMemberId}")
    public ResponseEntity<TeamMemberResponse> assignAlert(
            @PathVariable String alertId,
            @PathVariable String teamMemberId) {
        return ResponseEntity.ok(teamAssignmentService.assignAlert(alertId, teamMemberId));
    }

    @DeleteMapping("/incident/{incidentId}/unassign")
    public ResponseEntity<String> unassignIncident(@PathVariable String incidentId) {
        teamAssignmentService.unassignIncident(incidentId);
        return ResponseEntity.ok("Incident unassigned");
    }

    @DeleteMapping("/alert/{alertId}/unassign")
    public ResponseEntity<String> unassignAlert(@PathVariable String alertId) {
        teamAssignmentService.unassignAlert(alertId);
        return ResponseEntity.ok("Alert unassigned");
    }

    @GetMapping("/team-members/available")
    public ResponseEntity<List<TeamMemberResponse>> getAvailableTeamMembers() {
        return ResponseEntity.ok(teamAssignmentService.getAvailableTeamMembers());
    }

    @GetMapping("/team-members/specialization/{specialization}")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembersBySpecialization(
            @PathVariable String specialization) {
        return ResponseEntity.ok(teamAssignmentService.getTeamMembersBySpecialization(specialization));
    }
}
