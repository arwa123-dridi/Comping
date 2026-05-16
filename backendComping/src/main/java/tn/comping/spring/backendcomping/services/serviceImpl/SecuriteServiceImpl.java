package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.SecuriteRequest;
import tn.comping.spring.backendcomping.dto.SecuriteResponse;
import tn.comping.spring.backendcomping.entities.Securite;
import tn.comping.spring.backendcomping.repositories.SecuriteRepository;
import tn.comping.spring.backendcomping.utils.mapper.SecuriteMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecuriteServiceImpl implements SecuriteService {
    
    private final SecuriteRepository repository;
    private final SecuriteMapper mapper;
    
    @Override
    public SecuriteResponse creerMesure(SecuriteRequest request) {
        Securite securite = mapper.toEntity(request);
        securite.setDateDebut(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse getById(String id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Mesure de sécurité introuvable: " + id));
    }
    
    @Override
    public List<SecuriteResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getBySiteCampingId(String siteCampingId) {
        return repository.findBySiteCampingId(siteCampingId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getByStatut(String statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getByResponsable(String responsableId) {
        return repository.findByResponsableId(responsableId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getByNiveauSecurite(String niveauSecurite) {
        return repository.findByNiveauSecurite(niveauSecurite).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getByRiskLevel(String riskLevel) {
        return repository.findByRiskLevel(riskLevel).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getHighRiskMeasures() {
        return repository.findByRiskScoreGreaterThan(7).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SecuriteResponse> getLowSecurityScoreMeasures(Integer threshold) {
        return repository.findBySecurityScoreLessThan(threshold).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public SecuriteResponse updateMesure(String id, SecuriteRequest request) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        securite.setTitre(request.getTitre());
        securite.setDescription(request.getDescription());
        securite.setTypeMesure(request.getTypeMesure());
        securite.setNiveauSecurite(request.getNiveauSecurite());
        securite.setZoneSecurisee(request.getZoneSecurisee());
        securite.setMonitoringType(request.getMonitoringType());
        securite.setSecurityScore(request.getSecurityScore());
        securite.setRiskScore(request.getRiskScore());
        securite.setEquipmentUsed(request.getEquipmentUsed());
        securite.setMonitoringLocations(request.getMonitoringLocations());
        securite.setNotes(request.getNotes());
        securite.setDateModification(new Date());
        
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse updateStatut(String id, String statut) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        securite.setStatut(statut);
        if ("COMPLETEE".equals(statut)) {
            securite.setDateFin(new Date());
        }
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse assignTeamMember(String id, String memberId) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        if (securite.getTeamMemberIds() == null) {
            securite.setTeamMemberIds(new ArrayList<>());
        }
        if (!securite.getTeamMemberIds().contains(memberId)) {
            securite.getTeamMemberIds().add(memberId);
        }
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse removeTeamMember(String id, String memberId) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        if (securite.getTeamMemberIds() != null) {
            securite.getTeamMemberIds().remove(memberId);
        }
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse recordFinding(String id, String finding) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        if (securite.getFindings() == null) {
            securite.setFindings(new ArrayList<>());
        }
        securite.getFindings().add(finding);
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse addRecommendation(String id, String recommendation) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        if (securite.getRecommendations() == null) {
            securite.setRecommendations(new ArrayList<>());
        }
        securite.getRecommendations().add(recommendation);
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse recordIncident(String id, String incidentId) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        if (securite.getRelatedIncidentIds() == null) {
            securite.setRelatedIncidentIds(new ArrayList<>());
        }
        if (!securite.getRelatedIncidentIds().contains(incidentId)) {
            securite.getRelatedIncidentIds().add(incidentId);
            securite.setNumberOfIncidentsDetected(securite.getNumberOfIncidentsDetected() + 1);
        }
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse completeMonitoring(String id) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        securite.setLastMonitoringDate(new Date());
        securite.setMonitoringStatus("ACTIF");
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public SecuriteResponse updateBudgetUsed(String id, Double amount) {
        Securite securite = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesure introuvable: " + id));
        
        Double currentUsed = securite.getBudgetUtilise() != null ? securite.getBudgetUtilise() : 0.0;
        securite.setBudgetUtilise(currentUsed + amount);
        securite.setDateModification(new Date());
        return mapper.toResponse(repository.save(securite));
    }
    
    @Override
    public void deleteMesure(String id) {
        repository.deleteById(id);
    }
    
    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
    
    @Override
    public long count() {
        return repository.count();
    }
    
    @Override
    public long countByStatut(String statut) {
        return repository.countBySiteCampingIdAndStatut(null, statut);
    }
}
