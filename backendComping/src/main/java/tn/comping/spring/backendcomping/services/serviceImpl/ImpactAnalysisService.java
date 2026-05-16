package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.ImpactAnalysisResponse;
import tn.comping.spring.backendcomping.dto.PredictionResponse;
import java.util.List;

public interface ImpactAnalysisService {
    ImpactAnalysisResponse analyzeIncidentImpact(String incidentId);
    List<PredictionResponse> getPredictionsForIncident(String incidentId);
    void detectPatterns();
    List<tn.comping.spring.backendcomping.entities.IncidentPattern> getActivePatterns();
}
