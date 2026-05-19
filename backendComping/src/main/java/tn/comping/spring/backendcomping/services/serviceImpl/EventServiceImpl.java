package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.config.SecurityUtils;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.StatutEvent;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.utils.mapper.ActivityMapper;
import tn.comping.spring.backendcomping.utils.mapper.EventMapper;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ActivityRepository activityRepository;
    private final InteractionRepository interactionRepository;
    private final SecurityUtils securityUtils;
    private final CarteFideliteService carteFideliteService;

    @Override
    public EventResponseDTO createEvent(EventRequestDTO dto) {
        String userId = securityUtils.getCurrentUserId();
        Event event = EventMapper.toEntity(dto);
        event.setOrganisateurId(userId);
        event.setStatut(StatutEvent.EN_ATTENTE);
        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        
        // Record VIEW interaction
        recordInteraction(id, "VIEW_EVENT");
        
        return EventMapper.toDto(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(event -> {
                    EventResponseDTO dto = EventMapper.toDto(event);
                    if (event.getActivityIds() != null) {
                        dto.setActivities(
                                activityRepository.findAllById(event.getActivityIds())
                                        .stream()
                                        .map(ActivityMapper::toResponse)
                                        .toList()
                        );
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public EventResponseDTO updateEvent(String id, EventRequestDTO dto) {
        Event existing = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());
        existing.setPrix(dto.getPrix());
        existing.setCapacite(dto.getCapacite());
        if (dto.getStatut() != null) {
            existing.setStatut(dto.getStatut());
        }
        existing.setNiveauDifficulte(dto.getNiveauDifficulte());
        existing.setTrancheAge(dto.getTrancheAge());
        existing.setLatitude(dto.getLatitude());
        existing.setLongitude(dto.getLongitude());
        existing.setSaison(dto.getSaison());
        existing.setDureeEnHeures(dto.getDureeEnHeures());
        existing.setTags(dto.getTags());
        existing.setActivityIds(dto.getActivityIds());
        return EventMapper.toDto(eventRepository.save(existing));
    }

    @Override
    public void deleteEvent(String id) {
        eventRepository.deleteById(id);
    }

    @Override
    public long countByStatut(String statut) {
        return eventRepository.countByStatut(StatutEvent.valueOf(statut));
    }

    @Override
    public EventResponseDTO participate(String eventId) {
        String userId = securityUtils.getCurrentUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getParticipantIds() == null) {
            event.setParticipantIds(new ArrayList<>());
        }

        if (!event.getParticipantIds().contains(userId)) {
            event.getParticipantIds().add(userId);
            eventRepository.save(event);
            
            // Record JOIN interaction
            recordInteraction(eventId, "JOIN_EVENT");
            
            // Award loyalty points
            carteFideliteService.ajouterPoints(userId, 10);
        }

        return EventMapper.toDto(event);
    }

    @Override
    public EventResponseDTO cancelParticipation(String eventId) {
        String userId = securityUtils.getCurrentUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getParticipantIds() != null) {
            event.getParticipantIds().remove(userId);
            eventRepository.save(event);
            
            // Record LEAVE interaction
            recordInteraction(eventId, "LEAVE_EVENT");
        }

        return EventMapper.toDto(event);
    }

    @Override
    public long countEvents() {
        return eventRepository.count();
    }

    @Override
    public EventResponseDTO validerEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setStatut(StatutEvent.VALIDE);
        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO rejectEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setStatut(StatutEvent.ANNULE);
        return EventMapper.toDto(eventRepository.save(event));
    }

    private void recordInteraction(String eventId, String type) {
        try {
            String userId = securityUtils.getCurrentUserId();
            Interaction interaction = Interaction.builder()
                    .auteurId(userId)
                    .cibleId(eventId)
                    .cibleType("EVENT")
                    .type(type)
                    .dateCreation(new Date())
                    .build();
            interactionRepository.save(interaction);
        } catch (Exception e) {
            log.warn("Failed to record interaction: {}", e.getMessage());
        }
    }
}
