package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import java.util.List;

public interface DemandeTransportService {
    DemandeTransportResponse createDemandeTransport(DemandeTransportRequest dto);
    DemandeTransportResponse getDemandeTransportById(String id);
    List<DemandeTransportResponse> getAllDemandesTransport();
    DemandeTransportResponse updateDemandeTransport(String id, DemandeTransportRequest dto);
    void deleteDemandeTransport(String id);
    List<DemandeTransportResponse> getDemandesByUserId(String userId); // ← ajouté
}