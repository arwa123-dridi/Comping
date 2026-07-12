package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.dto.TraitementIncidentRequest;
import java.util.List;

public interface IncidentService {
    List<IncidentResponse> getAllIncidents();
    IncidentResponse getIncidentById(String id, String email);
    IncidentResponse createIncident(IncidentRequest request, String email);
    IncidentResponse updateIncident(String id, IncidentRequest request, String email);
    void deleteIncident(String id, String email);
    List<IncidentResponse> getMesIncidents(String email);
    IncidentResponse traiterIncident(String id, TraitementIncidentRequest dto, String email);
    IncidentResponse reclassifier(String id);
}
