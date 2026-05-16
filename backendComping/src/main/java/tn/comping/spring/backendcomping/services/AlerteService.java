package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface AlerteService {
    AlerteResponse declencherAlerte(AlerteRequest request);
    List<AlerteResponse> getAlertesBySite(String siteCampingId);
    List<AlerteResponse> getAllAlertes();
    void supprimerAlerte(String id);
    AlerteResponse updateStatut(String id, String statut);
}