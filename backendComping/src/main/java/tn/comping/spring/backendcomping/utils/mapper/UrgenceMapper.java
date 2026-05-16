package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.UrgenceRequest;
import tn.comping.spring.backendcomping.dto.UrgenceResponse;
import tn.comping.spring.backendcomping.entities.Urgence;
import java.util.Date;

@Component
public class UrgenceMapper {
    
    public Urgence toEntity(UrgenceRequest request) {
        if (request == null) return null;
        
        Urgence urgence = new Urgence();
        urgence.setTitre(request.getTitre());
        urgence.setDescription(request.getDescription());
        urgence.setSiteCampingId(request.getSiteCampingId());
        urgence.setUserId(request.getUserId());
        urgence.setNiveauUrgence(request.getNiveauUrgence());
        urgence.setEstimatedMinutesBeforeResolution(request.getEstimatedMinutesBeforeResolution());
        urgence.setCategorie(request.getCategorie());
        urgence.setPriorite(request.getPriorite());
        urgence.setReporterId(request.getReporterId());
        urgence.setImpactScore(request.getImpactScore());
        urgence.setEstimatedCost(request.getEstimatedCost());
        urgence.setContactName(request.getContactName());
        urgence.setContactPhone(request.getContactPhone());
        urgence.setContactEmail(request.getContactEmail());
        urgence.setLocation(request.getLocation());
        urgence.setTags(request.getTags());
        urgence.setNotes(request.getNotes());
        urgence.setDateCreation(new Date());
        urgence.setStatut("ATTENDANT");
        urgence.setNumberOfEscalations(0);
        
        // Parse latitude and longitude if location is provided
        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            try {
                String[] coords = request.getLocation().split(",");
                urgence.setLatitude(Double.parseDouble(coords[0].trim()));
                urgence.setLongitude(Double.parseDouble(coords[1].trim()));
            } catch (Exception e) {
                // Invalid format, skip
            }
        }
        
        return urgence;
    }
    
    public UrgenceResponse toResponse(Urgence urgence) {
        if (urgence == null) return null;
        
        UrgenceResponse response = new UrgenceResponse();
        response.setId(urgence.getId());
        response.setTitre(urgence.getTitre());
        response.setDescription(urgence.getDescription());
        response.setDateCreation(urgence.getDateCreation());
        response.setDateExpiration(urgence.getDateExpiration());
        response.setStatut(urgence.getStatut());
        response.setSiteCampingId(urgence.getSiteCampingId());
        response.setUserId(urgence.getUserId());
        response.setNiveauUrgence(urgence.getNiveauUrgence());
        response.setEstimatedMinutesBeforeResolution(urgence.getEstimatedMinutesBeforeResolution());
        response.setAssigneId(urgence.getAssigneId());
        response.setDateAssignment(urgence.getDateAssignment());
        response.setResolution(urgence.getResolution());
        response.setDateResolution(urgence.getDateResolution());
        response.setImpactScore(urgence.getImpactScore());
        response.setEstimatedCost(urgence.getEstimatedCost());
        response.setAffectedUsers(urgence.getAffectedUsers());
        response.setCategorie(urgence.getCategorie());
        response.setPriorite(urgence.getPriorite());
        response.setReporterId(urgence.getReporterId());
        response.setTags(urgence.getTags());
        response.setContactName(urgence.getContactName());
        response.setContactPhone(urgence.getContactPhone());
        response.setContactEmail(urgence.getContactEmail());
        response.setLocation(urgence.getLocation());
        response.setLatitude(urgence.getLatitude());
        response.setLongitude(urgence.getLongitude());
        response.setDateModification(urgence.getDateModification());
        response.setModifiedBy(urgence.getModifiedBy());
        response.setNumberOfEscalations(urgence.getNumberOfEscalations());
        response.setNotes(urgence.getNotes());
        response.setComments(urgence.getComments());
        
        return response;
    }
}
