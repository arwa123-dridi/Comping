package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface AlerteService {
    AlerteResponse declencherAlerte(AlerteRequest request);
    List<AlerteResponse> getAlertesBySite(String siteCampingId);
    List<AlerteResponse> getAlertesByCritere(String siteCampingId, String statut);
    List<AlerteResponse> getAllAlertes();
    void supprimerAlerte(String id);
    AlerteResponse updateStatut(String id, String statut);
    AlerteResponse prendreEnCharge(String id);
    AlerteResponse cloturer(String id);
}