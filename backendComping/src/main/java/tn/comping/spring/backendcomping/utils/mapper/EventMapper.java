package tn.comping.spring.backendcomping.utils.mapper;


import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.StatutEvent;

public class EventMapper {
    public static Event toEntity(EventRequestDTO dto) {
        if(dto == null) return null;
        return Event.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .capacite(dto.getCapacite())
                .statut(dto.getStatut() != null ? dto.getStatut() : StatutEvent.VALIDE)
                .activityIds(dto.getActivityIds())
                .build();
    }

    public static EventResponseDTO toDto(Event entity) {
        if(entity == null) return null;
        return EventResponseDTO.builder()
                .idEvent(entity.getIdEvent())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .prix(entity.getPrix())
                .capacite(entity.getCapacite())
                .statut(entity.getStatut())
                .build();
    }
}
