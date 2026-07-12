package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.StatutIncident;

import java.util.ArrayList;
import java.util.Date;

public class IncidentMapper {

    public static Incident toEntity(IncidentRequest dto, String userId) {
        if (dto == null) return null;
        return Incident.builder()
                .type(dto.getType())
                .statut(StatutIncident.OUVERT)
                .description(dto.getDescription())
                .dateDeclaration(new Date())
                .userId(userId)
                .demandeTransportId(dto.getDemandeTransportId())
                .historique(new ArrayList<>())
                .build();
    }

    public static void updateEntityFromDto(Incident entity, IncidentRequest dto) {
        entity.setType(dto.getType());
        entity.setDescription(dto.getDescription());
        entity.setDemandeTransportId(dto.getDemandeTransportId());
    }

    public static IncidentResponse toDto(Incident entity) {
        if (entity == null) return null;
        return IncidentResponse.builder()
                .idIncident(entity.getIdIncident())
                .type(entity.getType())
                .statut(entity.getStatut())
                .description(entity.getDescription())
                .dateDeclaration(entity.getDateDeclaration())
                .resolu(entity.isResolu())
                .userId(entity.getUserId())
                .priorite(entity.getPriorite())
                .commentaireOrganisateur(entity.getCommentaireOrganisateur())
                .dateTraitement(entity.getDateTraitement())
                .demandeTransportId(entity.getDemandeTransportId())
                .historique(entity.getHistorique())
                .build();
    }
}
