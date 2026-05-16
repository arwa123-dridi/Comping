package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.SecuriteRequest;
import tn.comping.spring.backendcomping.dto.SecuriteResponse;
import tn.comping.spring.backendcomping.entities.Securite;
import java.util.Date;

@Component
public class SecuriteMapper {
    
    public Securite toEntity(SecuriteRequest request) {
        if (request == null) return null;
        
        Securite securite = new Securite();
        securite.setTitre(request.getTitre());
        securite.setDescription(request.getDescription());
        securite.setSiteCampingId(request.getSiteCampingId());
        securite.setTypeMesure(request.getTypeMesure());
        securite.setNiveauSecurite(request.getNiveauSecurite());
        securite.setZoneSecurisee(request.getZoneSecurisee());
        securite.setResponsableId(request.getResponsableId());
        securite.setMonitoringType(request.getMonitoringType());
        securite.setSecurityScore(request.getSecurityScore());
        securite.setRiskScore(request.getRiskScore());
        securite.setBudgetAlloue(request.getBudgetAlloue());
        securite.setEquipmentUsed(request.getEquipmentUsed());
        securite.setMonitoringLocations(request.getMonitoringLocations());
        securite.setNotes(request.getNotes());
        securite.setDateCreation(new Date());
        securite.setStatut("PLANIFIEE");
        securite.setBudgetUtilise(0.0);
        securite.setNumberOfIncidentsDetected(0);
        
        // Set risk level based on risk score
        if (request.getRiskScore() != null) {
            if (request.getRiskScore() <= 3) {
                securite.setRiskLevel("LOW");
            } else if (request.getRiskScore() <= 6) {
                securite.setRiskLevel("MEDIUM");
            } else if (request.getRiskScore() <= 8) {
                securite.setRiskLevel("HIGH");
            } else {
                securite.setRiskLevel("CRITICAL");
            }
        }
        
        return securite;
    }
    
    public SecuriteResponse toResponse(Securite securite) {
        if (securite == null) return null;
        
        SecuriteResponse response = new SecuriteResponse();
        response.setId(securite.getId());
        response.setTitre(securite.getTitre());
        response.setDescription(securite.getDescription());
        response.setDateCreation(securite.getDateCreation());
        response.setDateDebut(securite.getDateDebut());
        response.setDateFin(securite.getDateFin());
        response.setStatut(securite.getStatut());
        response.setSiteCampingId(securite.getSiteCampingId());
        response.setTypeMesure(securite.getTypeMesure());
        response.setNiveauSecurite(securite.getNiveauSecurite());
        response.setZoneSecurisee(securite.getZoneSecurisee());
        response.setResponsableId(securite.getResponsableId());
        response.setTeamMemberIds(securite.getTeamMemberIds());
        response.setDateAssignment(securite.getDateAssignment());
        response.setEquipmentUsed(securite.getEquipmentUsed());
        response.setResourcesNeeded(securite.getResourcesNeeded());
        response.setBudgetAlloue(securite.getBudgetAlloue());
        response.setBudgetUtilise(securite.getBudgetUtilise());
        response.setRelatedIncidentIds(securite.getRelatedIncidentIds());
        response.setRelatedAlerteIds(securite.getRelatedAlerteIds());
        response.setNumberOfIncidentsDetected(securite.getNumberOfIncidentsDetected());
        response.setConformiteAudit(securite.getConformiteAudit());
        response.setCertificateNumber(securite.getCertificateNumber());
        response.setCertificateExpiry(securite.getCertificateExpiry());
        response.setComplianceStatus(securite.getComplianceStatus());
        response.setMonitoringType(securite.getMonitoringType());
        response.setMonitoringLocations(securite.getMonitoringLocations());
        response.setLastMonitoringDate(securite.getLastMonitoringDate());
        response.setMonitoringStatus(securite.getMonitoringStatus());
        response.setSecurityScore(securite.getSecurityScore());
        response.setRiskScore(securite.getRiskScore());
        response.setRiskLevel(securite.getRiskLevel());
        response.setFindings(securite.getFindings());
        response.setRecommendations(securite.getRecommendations());
        response.setActionTaken(securite.getActionTaken());
        response.setDateActionTaken(securite.getDateActionTaken());
        response.setReportedBy(securite.getReportedBy());
        response.setApprovedBy(securite.getApprovedBy());
        response.setNotes(securite.getNotes());
        response.setDateModification(securite.getDateModification());
        
        return response;
    }
}
