package tn.comping.spring.backendcomping.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.services.AdvancedSecurityWorkflowService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advanced-workflows")
@CrossOrigin("*")
public class AdvancedWorkflowController {

    @Autowired
    private AdvancedSecurityWorkflowService workflowService;

    // 1. Automatic Alert Escalation Workflow
    @PostMapping("/escalate-alerts")
    public List<Urgence> triggerAlertEscalation() {
        return workflowService.escalatePendingAlerts();
    }

    // 2. Smart Security Dispatching
    @PostMapping("/dispatch-security/{urgenceId}")
    public Securite dispatchSecurity(@PathVariable String urgenceId) {
        return workflowService.dispatchSecurityForUrgence(urgenceId);
    }

    // 3. Predictive AI Security Analysis
    @GetMapping("/predictive-analysis/{siteId}")
    public Map<String, String> getPredictiveAnalysis(@PathVariable String siteId) {
        String analysis = workflowService.runPredictiveAnalysis(siteId);
        return Map.of("recommendations", analysis);
    }

    // 4. Campus-wide Emergency Broadcast
    @PostMapping("/broadcast")
    public Map<String, String> broadcastEmergency(@RequestBody Map<String, String> payload) {
        String message = payload.getOrDefault("message", "CODE ROUGE - EVACUATION");
        workflowService.broadcastCodeRed(message);
        return Map.of("status", "Broadcast envoyé");
    }

    // 5. Geofencing & Proximity Alerts
    @PostMapping("/check-geofence")
    public Alerte checkGeofence(@RequestBody Map<String, Object> payload) {
        Double lat = Double.valueOf(payload.get("lat").toString());
        Double lon = Double.valueOf(payload.get("lon").toString());
        String siteId = (String) payload.get("siteId");
        String userId = (String) payload.get("userId");
        return workflowService.checkGeofenceBreach(lat, lon, siteId, userId);
    }

    // 6. Automated Post-Mortem Reports
    @PostMapping("/post-mortem/{urgenceId}")
    public Map<String, String> generatePostMortem(@PathVariable String urgenceId) {
        String report = workflowService.generatePostMortem(urgenceId);
        return Map.of("report", report);
    }
}
