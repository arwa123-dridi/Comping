package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.SecuriteRequest;
import tn.comping.spring.backendcomping.dto.SecuriteResponse;
import java.util.List;

public interface SecuriteService {
    SecuriteResponse creerMesure(SecuriteRequest request);
    SecuriteResponse getById(String id);
    List<SecuriteResponse> getAll();
    List<SecuriteResponse> getBySiteCampingId(String siteCampingId);
    List<SecuriteResponse> getByStatut(String statut);
    List<SecuriteResponse> getByResponsable(String responsableId);
    List<SecuriteResponse> getByNiveauSecurite(String niveauSecurite);
    List<SecuriteResponse> getByRiskLevel(String riskLevel);
    List<SecuriteResponse> getHighRiskMeasures();
    List<SecuriteResponse> getLowSecurityScoreMeasures(Integer threshold);
    SecuriteResponse updateMesure(String id, SecuriteRequest request);
    SecuriteResponse updateStatut(String id, String statut);
    SecuriteResponse assignTeamMember(String id, String memberId);
    SecuriteResponse removeTeamMember(String id, String memberId);
    SecuriteResponse recordFinding(String id, String finding);
    SecuriteResponse addRecommendation(String id, String recommendation);
    SecuriteResponse recordIncident(String id, String incidentId);
    SecuriteResponse completeMonitoring(String id);
    SecuriteResponse updateBudgetUsed(String id, Double amount);
    void deleteMesure(String id);
    void deleteAll();
    long count();
    long countByStatut(String statut);
}
