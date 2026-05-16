package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.UrgenceRequest;
import tn.comping.spring.backendcomping.dto.UrgenceResponse;
import java.util.List;

public interface UrgenceService {
    UrgenceResponse creerUrgence(UrgenceRequest request);
    UrgenceResponse getById(String id);
    List<UrgenceResponse> getAll();
    List<UrgenceResponse> getBySiteCampingId(String siteCampingId);
    List<UrgenceResponse> getByStatut(String statut);
    List<UrgenceResponse> getByNiveauUrgence(String niveauUrgence);
    List<UrgenceResponse> getByAssignee(String assigneId);
    List<UrgenceResponse> getByCategory(String categorie);
    List<UrgenceResponse> getByUserId(String userId);
    UrgenceResponse updateUrgence(String id, UrgenceRequest request);
    UrgenceResponse updateStatut(String id, String statut);
    UrgenceResponse assignTo(String id, String assigneId);
    UrgenceResponse resolveUrgence(String id, String resolution);
    UrgenceResponse rejectUrgence(String id, String reason);
    UrgenceResponse complete(String id);
    UrgenceResponse addComment(String id, String comment);
    void deleteUrgence(String id);
    void deleteAll();
    long count();
    long countByStatut(String statut);
}
