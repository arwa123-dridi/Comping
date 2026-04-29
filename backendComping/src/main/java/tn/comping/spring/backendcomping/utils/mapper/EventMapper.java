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
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .dureeEnHeures(dto.getDureeEnHeures())

                .tags(dto.getTags())
                .niveauDifficulte(dto.getNiveauDifficulte())
                .trancheAge(dto.getTrancheAge())
                .saison(dto.getSaison())
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
                .niveauDifficulte(entity.getNiveauDifficulte())
                .trancheAge(entity.getTrancheAge())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .saison(entity.getSaison())
                .dureeEnHeures(entity.getDureeEnHeures())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
