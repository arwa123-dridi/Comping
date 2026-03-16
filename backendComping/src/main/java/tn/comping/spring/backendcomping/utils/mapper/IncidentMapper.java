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
                .build();
    }
}