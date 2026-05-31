package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import java.util.List;

public interface IncidentService {
    List<IncidentResponse> getAllIncidents();
    IncidentResponse getIncidentById(String id);
    IncidentResponse createIncident(IncidentRequest request);
    IncidentResponse updateIncident(String id, IncidentRequest request);
    void deleteIncident(String id);
    List<IncidentResponse> getIncidentsByUserId(String userId);
}