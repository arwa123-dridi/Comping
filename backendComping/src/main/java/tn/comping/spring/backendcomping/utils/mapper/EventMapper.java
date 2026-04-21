package tn.comping.spring.backendcomping.utils.mapper;


import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.StatutEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .lieu(dto.getLieu())


                .participantIds(new ArrayList<>())


                .categorie(dto.getCategorie())

                .createdAt(LocalDateTime.now())
                .build();
    }

    public static EventResponseDTO toDto(Event entity) {
        if(entity == null) return null;
        int nbParticipants = entity.getParticipantIds() != null
                ? entity.getParticipantIds().size()
                : 0;
        return EventResponseDTO.builder()
                .idEvent(entity.getIdEvent())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .prix(entity.getPrix())
                .capacite(entity.getCapacite())
                .statut(entity.getStatut())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .lieu(entity.getLieu())

                .organisateurId(entity.getOrganisateurId())


                .nombreParticipants(nbParticipants)
                .placesRestantes(entity.getCapacite() - nbParticipants)
                .dejaInscrit(false)
                .categorie(entity.getCategorie())

                .createdAt(entity.getCreatedAt())
                .build();
    }
}
