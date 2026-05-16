package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.UrgenceRequest;
import tn.comping.spring.backendcomping.dto.UrgenceResponse;
import tn.comping.spring.backendcomping.entities.Urgence;
import tn.comping.spring.backendcomping.repositories.UrgenceRepository;
import tn.comping.spring.backendcomping.utils.mapper.UrgenceMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrgenceServiceImpl implements UrgenceService {
    
    private final UrgenceRepository repository;
    private final UrgenceMapper mapper;
    
    @Override
    public UrgenceResponse creerUrgence(UrgenceRequest request) {
        Urgence urgence = mapper.toEntity(request);
        urgence.setDateExpiration(new Date(System.currentTimeMillis() + 
                                           request.getEstimatedMinutesBeforeResolution() * 60 * 1000L));
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse getById(String id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
    }
    
    @Override
    public List<UrgenceResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getBySiteCampingId(String siteCampingId) {
        return repository.findBySiteCampingId(siteCampingId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getByStatut(String statut) {
        return repository.findByStatut(statut).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getByNiveauUrgence(String niveauUrgence) {
        return repository.findByNiveauUrgence(niveauUrgence).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getByAssignee(String assigneId) {
        return repository.findByAssigneId(assigneId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getByCategory(String categorie) {
        return repository.findByCategorie(categorie).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UrgenceResponse> getByUserId(String userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public UrgenceResponse updateUrgence(String id, UrgenceRequest request) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        urgence.setTitre(request.getTitre());
        urgence.setDescription(request.getDescription());
        urgence.setNiveauUrgence(request.getNiveauUrgence());
        urgence.setEstimatedMinutesBeforeResolution(request.getEstimatedMinutesBeforeResolution());
        urgence.setCategorie(request.getCategorie());
        urgence.setPriorite(request.getPriorite());
        urgence.setImpactScore(request.getImpactScore());
        urgence.setEstimatedCost(request.getEstimatedCost());
        urgence.setContactName(request.getContactName());
        urgence.setContactPhone(request.getContactPhone());
        urgence.setContactEmail(request.getContactEmail());
        urgence.setLocation(request.getLocation());
        urgence.setTags(request.getTags());
        urgence.setNotes(request.getNotes());
        urgence.setDateModification(new Date());
        
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse updateStatut(String id, String statut) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        urgence.setStatut(statut);
        urgence.setDateModification(new Date());
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse assignTo(String id, String assigneId) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        urgence.setAssigneId(assigneId);
        urgence.setDateAssignment(new Date());
        urgence.setStatut("ACCEPTE");
        urgence.setDateModification(new Date());
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse resolveUrgence(String id, String resolution) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        urgence.setResolution(resolution);
        urgence.setDateResolution(new Date());
        urgence.setStatut("COMPLETEE");
        urgence.setDateModification(new Date());
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse rejectUrgence(String id, String reason) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        urgence.setResolution("Rejetée: " + reason);
        urgence.setStatut("REJETEE");
        urgence.setDateModification(new Date());
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public UrgenceResponse complete(String id) {
        return updateStatut(id, "COMPLETEE");
    }
    
    @Override
    public UrgenceResponse addComment(String id, String comment) {
        Urgence urgence = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Urgence introuvable: " + id));
        
        if (urgence.getComments() == null) {
            urgence.setComments(new ArrayList<>());
        }
        urgence.getComments().add(comment);
        urgence.setDateModification(new Date());
        return mapper.toResponse(repository.save(urgence));
    }
    
    @Override
    public void deleteUrgence(String id) {
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
        return repository.findByStatut(statut).size();
    }
}
