package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EscalationEventResponse;
import java.util.List;

public interface EscalationService {
    void checkAndEscalateIncidents();
    void checkAndEscalateAlerts();
    List<EscalationEventResponse> getEscalationHistory(String incidentOrAlertId);
    EscalationEventResponse acknowledgeEscalation(String escalationEventId);
    List<EscalationEventResponse> getPendingEscalations();
}
