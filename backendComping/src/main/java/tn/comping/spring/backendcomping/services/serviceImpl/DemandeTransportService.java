package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.dto.TraitementDemandeRequest;
import java.util.List;

public interface DemandeTransportService {
    DemandeTransportResponse createDemandeTransport(DemandeTransportRequest dto, String email);
    DemandeTransportResponse getDemandeTransportById(String id, String email);
    List<DemandeTransportResponse> getAllDemandesTransport();
    List<DemandeTransportResponse> getMesDemandes(String email);
    DemandeTransportResponse updateDemandeTransport(String id, DemandeTransportRequest dto, String email);
    void deleteDemandeTransport(String id, String email);
    DemandeTransportResponse traiterDemande(String id, TraitementDemandeRequest dto, String email);
}
