package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.CreneauLivraisonRequest;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonResponse;
import java.util.List;

public interface CreneauLivraisonService {
    List<CreneauLivraisonResponse> getAllCreneaux();
    CreneauLivraisonResponse getCreneauById(String id);
    CreneauLivraisonResponse createCreneau(CreneauLivraisonRequest request);
    CreneauLivraisonResponse updateCreneau(String id, CreneauLivraisonRequest request);
    void deleteCreneau(String id);
}