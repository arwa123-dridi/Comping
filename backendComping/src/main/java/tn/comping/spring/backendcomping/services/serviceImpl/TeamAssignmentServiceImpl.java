package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.AssignmentSuggestionResponse;
import tn.comping.spring.backendcomping.dto.TeamMemberResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.Alerte;
import tn.comping.spring.backendcomping.entities.TeamMember;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.repositories.AlerteRepository;
import tn.comping.spring.backendcomping.repositories.TeamMemberRepository;
import tn.comping.spring.backendcomping.utils.mapper.TeamMemberMapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamAssignmentServiceImpl implements TeamAssignmentService {

    private final TeamMemberRepository teamMemberRepository;
    private final IncidentRepository incidentRepository;
    private final AlerteRepository alerteRepository;
    private final TeamMemberMapper mapper;

    @Override
    public AssignmentSuggestionResponse suggestAssignmentForIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        List<TeamMember> candidates = findCandidatesForIncident(incident);
        
        if (candidates.isEmpty()) {
            return AssignmentSuggestionResponse.builder()
                    .incidentOrAlertId(incidentId)
                    .hasSuggestion(false)
                    .reason("No available team members with matching expertise")
                    .build();
        }

        // Sort by workload (least busy first)
        TeamMember suggested = candidates.stream()
                .min(Comparator.comparingInt(tm -> tm.getActiveIncidentIds().size()))
                .orElse(candidates.get(0));

        return AssignmentSuggestionResponse.builder()
                .incidentOrAlertId(incidentId)
                .hasSuggestion(true)
                .suggestedTeamMemberId(suggested.getId())
                .suggestedTeamMemberName(suggested.getName())
                .reason("Selected based on least workload: " + suggested.getActiveIncidentIds().size() + " active incidents")
                .confidenceScore(0.85)
                .build();
    }

    @Override
    public AssignmentSuggestionResponse suggestAssignmentForAlert(String alertId) {
        Alerte alerte = alerteRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        List<TeamMember> candidates = findCandidatesForAlert(alerte);
        
        if (candidates.isEmpty()) {
            return AssignmentSuggestionResponse.builder()
                    .incidentOrAlertId(alertId)
                    .hasSuggestion(false)
                    .reason("No available team members")
                    .build();
        }

        // Sort by workload
        TeamMember suggested = candidates.stream()
                .min(Comparator.comparingInt(tm -> tm.getActiveAlertIds().size()))
                .orElse(candidates.get(0));

        return AssignmentSuggestionResponse.builder()
                .incidentOrAlertId(alertId)
                .hasSuggestion(true)
                .suggestedTeamMemberId(suggested.getId())
                .suggestedTeamMemberName(suggested.getName())
                .reason("Selected based on availability and load")
                .confidenceScore(0.80)
                .build();
    }

    @Override
    public TeamMemberResponse assignIncident(String incidentId, String teamMemberId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new RuntimeException("Team member not found: " + teamMemberId));

        // Check if under capacity
        if (member.getActiveIncidentIds().size() >= member.getMaxConcurrentIncidents()) {
            throw new RuntimeException("Team member is at maximum capacity");
        }

        // Assign
        incident.setAssigneId(teamMemberId);
        incidentRepository.save(incident);

        member.getActiveIncidentIds().add(incidentId);
        teamMemberRepository.save(member);

        log.info("Incident {} assigned to team member {}", incidentId, teamMemberId);
        return mapper.toResponse(member);
    }

    @Override
    public TeamMemberResponse assignAlert(String alertId, String teamMemberId) {
        Alerte alerte = alerteRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new RuntimeException("Team member not found: " + teamMemberId));

        // Check if under capacity
        if (member.getActiveAlertIds().size() >= member.getMaxConcurrentAlerts()) {
            throw new RuntimeException("Team member is at maximum capacity for alerts");
        }

        // Assign
        alerte.setAssigneId(teamMemberId);
        alerteRepository.save(alerte);

        member.getActiveAlertIds().add(alertId);
        teamMemberRepository.save(member);

        log.info("Alert {} assigned to team member {}", alertId, teamMemberId);
        return mapper.toResponse(member);
    }

    @Override
    public void unassignIncident(String incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found: " + incidentId));

        if (incident.getAssigneId() != null) {
            TeamMember member = teamMemberRepository.findById(incident.getAssigneId()).orElse(null);
            if (member != null) {
                member.getActiveIncidentIds().remove(incidentId);
                teamMemberRepository.save(member);
            }
        }

        incident.setAssigneId(null);
        incidentRepository.save(incident);
    }

    @Override
    public void unassignAlert(String alertId) {
        Alerte alerte = alerteRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        if (alerte.getAssigneId() != null) {
            TeamMember member = teamMemberRepository.findById(alerte.getAssigneId()).orElse(null);
            if (member != null) {
                member.getActiveAlertIds().remove(alertId);
                teamMemberRepository.save(member);
            }
        }

        alerte.setAssigneId(null);
        alerteRepository.save(alerte);
    }

    @Override
    public List<TeamMemberResponse> getAvailableTeamMembers() {
        return teamMemberRepository.findByAvailableIsTrue().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberResponse> getTeamMembersBySpecialization(String specialization) {
        return teamMemberRepository.findBySpecializationsContaining(specialization).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private List<TeamMember> findCandidatesForIncident(Incident incident) {
        // Find team members with matching specialization/category
        String category = incident.getCategorie();
        if (category != null && !category.isEmpty()) {
            return teamMemberRepository.findBySpecializationsContaining(category).stream()
                    .filter(TeamMember::getAvailable)
                    .collect(Collectors.toList());
        }
        // Fall back to all available
        return teamMemberRepository.findByAvailableIsTrue();
    }

    private List<TeamMember> findCandidatesForAlert(Alerte alerte) {
        // Find team members with appropriate level for alert type
        return teamMemberRepository.findByAvailableIsTrue();
    }
}
