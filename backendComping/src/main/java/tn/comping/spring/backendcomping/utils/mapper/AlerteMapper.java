package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Alerte;

@Component
public class AlerteMapper {
    public Alerte toEntity(AlerteRequest req) {
        Alerte a = new Alerte();
        a.setSiteCampingId(req.getSiteCampingId());
        a.setType(req.getType());
        a.setTitre(req.getTitre());
        a.setDescription(req.getDescription());
        a.setPosition(req.getPosition());
        a.setPriorite(req.getPriorite());
        a.setAssigneId(req.getAssigneId());
        a.setReporterId(req.getReporterId());
        a.setAffectedUsers(req.getAffectedUsers());
        a.setEquipmentAffected(req.getEquipmentAffected());
        a.setEstimatedCost(req.getEstimatedCost());
        a.setAttachments(req.getAttachments());
        return a;
    }

    public AlerteResponse toResponse(Alerte a) {
        AlerteResponse response = new AlerteResponse();
        response.setId(a.getId());
        response.setSiteCampingId(a.getSiteCampingId());
        response.setType(a.getType());
        response.setTitre(a.getTitre());
        response.setDescription(a.getDescription());
        response.setDateDeclenchement(a.getDateDeclenchement());
        response.setStatut(a.getStatut());
        response.setPosition(a.getPosition());
        response.setPriorite(a.getPriorite());
        response.setAssigneId(a.getAssigneId());
        response.setDateResolution(a.getDateResolution());
        response.setResolution(a.getResolution());
        response.setResponseTimeMinutes(a.getResponseTimeMinutes());
        response.setReporterId(a.getReporterId());
        response.setAffectedUsers(a.getAffectedUsers());
        response.setEquipmentAffected(a.getEquipmentAffected());
        response.setEstimatedCost(a.getEstimatedCost());
        response.setAttachments(a.getAttachments());
        response.setEscalationNotes(a.getEscalationNotes());
        return response;
    }
}