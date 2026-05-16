package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.EscalationEventResponse;
import tn.comping.spring.backendcomping.entities.EscalationEvent;
import tn.comping.spring.backendcomping.entities.EscalationRule;
import tn.comping.spring.backendcomping.repositories.EscalationEventRepository;
import tn.comping.spring.backendcomping.repositories.EscalationRuleRepository;
import tn.comping.spring.backendcomping.repositories.IncidentRepository;
import tn.comping.spring.backendcomping.repositories.AlerteRepository;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.Alerte;
import tn.comping.spring.backendcomping.utils.mapper.EscalationEventMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscalationServiceImpl implements EscalationService {

    private final EscalationEventRepository escalationEventRepository;
    private final EscalationRuleRepository escalationRuleRepository;
    private final IncidentRepository incidentRepository;
    private final AlerteRepository alerteRepository;
    private final EscalationEventMapper mapper;

    @Override
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void checkAndEscalateIncidents() {
        log.info("Checking incidents for escalation...");
        
        List<EscalationRule> rules = escalationRuleRepository.findByEnabledIsTrue().stream()
                .filter(r -> r.getSourceType().equals("INCIDENT") || r.getSourceType().equals("BOTH"))
                .collect(Collectors.toList());

        for (EscalationRule rule : rules) {
            checkIncidentsByRule(rule);
        }
    }

    @Override
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void checkAndEscalateAlerts() {
        log.info("Checking alerts for escalation...");
        
        List<EscalationRule> rules = escalationRuleRepository.findByEnabledIsTrue().stream()
                .filter(r -> r.getSourceType().equals("ALERT") || r.getSourceType().equals("BOTH"))
                .collect(Collectors.toList());

        for (EscalationRule rule : rules) {
            checkAlertsByRule(rule);
        }
    }

    private void checkIncidentsByRule(EscalationRule rule) {
        List<Incident> incidents = incidentRepository.findAll();
        
        for (Incident incident : incidents) {
            // Skip if already resolved or matching criteria not met
            if (incident.isResolu()) continue;
            if (!incident.getStatut().equals("OUVERT")) continue;
            
            // Check priority match
            if (rule.getTriggerPriority() != null && !incident.getPriorite().equals(rule.getTriggerPriority())) {
                continue;
            }
            
            // Check if already escalated
            List<EscalationEvent> history = escalationEventRepository.findByIncidentOrAlertId(incident.getIdIncident());
            if (!history.isEmpty()) continue;
            
            // Check time condition
            if (incident.getDateDeclaration() != null && rule.getMinutesBeforeEscalation() != null) {
                long minutesElapsed = java.time.temporal.ChronoUnit.MINUTES.between(
                        new java.sql.Timestamp(incident.getDateDeclaration().getTime()).toLocalDateTime(),
                        LocalDateTime.now()
                );
                
                if (minutesElapsed >= rule.getMinutesBeforeEscalation()) {
                    performEscalation(incident.getIdIncident(), "INCIDENT", rule);
                }
            }
        }
    }

    private void checkAlertsByRule(EscalationRule rule) {
        List<Alerte> alertes = alerteRepository.findAll();
        
        for (Alerte alerte : alertes) {
            // Skip if closed or matching criteria not met
            if (alerte.getStatut().equals("CLOTUREE")) continue;
            if (!alerte.getStatut().equals("ACTIVE")) continue;
            
            // Check priority match
            if (rule.getTriggerPriority() != null && !alerte.getPriorite().equals(rule.getTriggerPriority())) {
                continue;
            }
            
            // Check if already escalated
            List<EscalationEvent> history = escalationEventRepository.findByIncidentOrAlertId(alerte.getId());
            if (!history.isEmpty()) continue;
            
            // Check time condition
            if (alerte.getDateDeclenchement() != null && rule.getMinutesBeforeEscalation() != null) {
                long minutesElapsed = java.time.temporal.ChronoUnit.MINUTES.between(
                        new java.sql.Timestamp(alerte.getDateDeclenchement().getTime()).toLocalDateTime(),
                        LocalDateTime.now()
                );
                
                if (minutesElapsed >= rule.getMinutesBeforeEscalation()) {
                    performEscalation(alerte.getId(), "ALERT", rule);
                }
            }
        }
    }

    private void performEscalation(String incidentOrAlertId, String sourceType, EscalationRule rule) {
        EscalationEvent escalation = EscalationEvent.builder()
                .incidentOrAlertId(incidentOrAlertId)
                .sourceType(sourceType)
                .escalationTime(LocalDateTime.now())
                .toLevel(rule.getTriggerPriority())
                .reason(rule.getDescription())
                .escalatedToUserId(rule.getEscalateToUserId())
                .escalatedToRole(rule.getEscalateToRole())
                .notificationAcknowledged(false)
                .build();
        
        escalationEventRepository.save(escalation);
        log.info("Escalation performed for {} ID: {}", sourceType, incidentOrAlertId);
        // TODO: Send notification via email/SMS based on rule.notificationMethod
    }

    @Override
    public List<EscalationEventResponse> getEscalationHistory(String incidentOrAlertId) {
        return escalationEventRepository.findByIncidentOrAlertId(incidentOrAlertId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EscalationEventResponse acknowledgeEscalation(String escalationEventId) {
        EscalationEvent event = escalationEventRepository.findById(escalationEventId)
                .orElseThrow(() -> new RuntimeException("Escalation event not found: " + escalationEventId));
        event.setNotificationAcknowledged(true);
        return mapper.toResponse(escalationEventRepository.save(event));
    }

    @Override
    public List<EscalationEventResponse> getPendingEscalations() {
        return escalationEventRepository.findByNotificationAcknowledgedIsFalse().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
