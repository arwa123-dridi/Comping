package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ImpactAnalysisResponse;
import tn.comping.spring.backendcomping.dto.PredictionResponse;
import tn.comping.spring.backendcomping.entities.IncidentPattern;
import tn.comping.spring.backendcomping.services.serviceImpl.ImpactAnalysisService;
import java.util.List;

@RestController
@RequestMapping("/api/impact-analysis")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ImpactAnalysisController {

    private final ImpactAnalysisService impactAnalysisService;

    @GetMapping("/analyze/{incidentId}")
    public ResponseEntity<ImpactAnalysisResponse> analyzeIncidentImpact(
            @PathVariable String incidentId) {
        return ResponseEntity.ok(impactAnalysisService.analyzeIncidentImpact(incidentId));
    }

    @GetMapping("/predictions/{incidentId}")
    public ResponseEntity<List<PredictionResponse>> getPredictionsForIncident(
            @PathVariable String incidentId) {
        return ResponseEntity.ok(impactAnalysisService.getPredictionsForIncident(incidentId));
    }

    @PostMapping("/detect-patterns")
    public ResponseEntity<String> detectPatterns() {
        impactAnalysisService.detectPatterns();
        return ResponseEntity.ok("Pattern detection completed");
    }

    @GetMapping("/active-patterns")
    public ResponseEntity<List<IncidentPattern>> getActivePatterns() {
        return ResponseEntity.ok(impactAnalysisService.getActivePatterns());
    }
}
