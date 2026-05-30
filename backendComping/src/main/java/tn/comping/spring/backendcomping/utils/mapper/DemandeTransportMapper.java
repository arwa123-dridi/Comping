package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.entities.DemandeTransport;

import java.util.Date;

public class DemandeTransportMapper {

    public static DemandeTransport toEntity(DemandeTransportRequest dto) {
        if (dto == null) return null;

        return DemandeTransport.builder()
                .dateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : new Date())
                .statut(dto.getStatut())
                .typeService(dto.getTypeService())
                .userId(dto.getUserId()) // ← ajouté
                .build();
    }

    public static DemandeTransportResponse toDto(DemandeTransport entity) {
        if (entity == null) return null;

        return DemandeTransportResponse.builder()
                .idDemandeTransport(entity.getIdDemandeTransport())
                .dateCreation(entity.getDateCreation())
                .statut(entity.getStatut())
                .typeService(entity.getTypeService())
                .userId(entity.getUserId()) // ← ajouté
                .build();
    }
}