package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import java.util.Date;

public class IncidentMapper {

    public static Incident toEntity(IncidentRequest dto) {
        if (dto == null) return null;

        return Incident.builder()
                .type(dto.getType())
                .statut(dto.getStatut())
                .descrition(dto.getDescrition())
                .dateDeclaration(dto.getDateDeclaration() != null ? dto.getDateDeclaration() : new Date())
                .resolu(dto.isResolu())
                .priorite(dto.getPriorite())
                .assigneId(dto.getAssigneId())
                .categorie(dto.getCategorie())
                .estimatedResolutionMinutes(dto.getEstimatedResolutionMinutes())
                .resolution(dto.getResolution())
                .impactScore(dto.getImpactScore())
                .tags(dto.getTags())
                .location(dto.getLocation())
                .reporterId(dto.getReporterId())
                .build();
    }

    public static IncidentResponse toDto(Incident entity) {
        if (entity == null) return null;

        return IncidentResponse.builder()
                .idIncident(entity.getIdIncident())
                .type(entity.getType())
                .statut(entity.getStatut())
                .descrition(entity.getDescrition())
                .dateDeclaration(entity.getDateDeclaration())
                .resolu(entity.isResolu())
                .priorite(entity.getPriorite())
                .assigneId(entity.getAssigneId())
                .categorie(entity.getCategorie())
                .estimatedResolutionMinutes(entity.getEstimatedResolutionMinutes())
                .dateResolution(entity.getDateResolution())
                .resolution(entity.getResolution())
                .impactScore(entity.getImpactScore())
                .tags(entity.getTags())
                .location(entity.getLocation())
                .reporterId(entity.getReporterId())
                .build();
    }
}