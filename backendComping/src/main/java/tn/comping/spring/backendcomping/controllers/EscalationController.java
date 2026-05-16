package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.EscalationEventResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.EscalationService;
import java.util.List;

@RestController
@RequestMapping("/api/escalations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class EscalationController {

    private final EscalationService escalationService;

    @GetMapping("/history/{incidentOrAlertId}")
    public ResponseEntity<List<EscalationEventResponse>> getEscalationHistory(
            @PathVariable String incidentOrAlertId) {
        return ResponseEntity.ok(escalationService.getEscalationHistory(incidentOrAlertId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<EscalationEventResponse>> getPendingEscalations() {
        return ResponseEntity.ok(escalationService.getPendingEscalations());
    }

    @PatchMapping("/{escalationEventId}/acknowledge")
    public ResponseEntity<EscalationEventResponse> acknowledgeEscalation(
            @PathVariable String escalationEventId) {
        return ResponseEntity.ok(escalationService.acknowledgeEscalation(escalationEventId));
    }

    @PostMapping("/check-incidents")
    public ResponseEntity<String> checkAndEscalateIncidents() {
        escalationService.checkAndEscalateIncidents();
        return ResponseEntity.ok("Incident escalation check completed");
    }

    @PostMapping("/check-alerts")
    public ResponseEntity<String> checkAndEscalateAlerts() {
        escalationService.checkAndEscalateAlerts();
        return ResponseEntity.ok("Alert escalation check completed");
    }
}
