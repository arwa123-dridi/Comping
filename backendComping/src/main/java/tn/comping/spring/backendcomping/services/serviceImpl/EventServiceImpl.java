package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.config.JwtUtils;
import tn.comping.spring.backendcomping.config.SecurityUtils;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.utils.mapper.ActivityMapper;
import tn.comping.spring.backendcomping.utils.mapper.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements  EventService{
    private final EventRepository eventRepository;
    private final ActivityRepository activityRepository;
    private final SecurityUtils securityUtils;
    @Override
    public EventResponseDTO createEvent(EventRequestDTO dto) {
        String userId = securityUtils.getCurrentUserId();
        Event event = EventMapper.toEntity(dto);
        event.setOrganisateurId(userId);
        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public EventResponseDTO getEventById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
        return EventMapper.toDto(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(event -> {

                    EventResponseDTO dto = EventMapper.toDto(event);

                    dto.setActivities(
                            activityRepository.findAllById(event.getActivityIds())
                                    .stream()
                                    .map(ActivityMapper::toResponse)
                                    .toList()
                    );

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
            if (!eventRepository.existsById(id)) {
                throw new RuntimeException("Event not found with id: " + id);
            }
            eventRepository.deleteById(id);

    }

    @Override
    public long countByStatut(String statut) {
        return eventRepository.countByStatut(statut);
    }

    @Override
    public EventResponseDTO participate(String eventId) {
        // 1. Récupérer user connecté
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        if (event.getParticipantIds() == null) {
            event.setParticipantIds(new java.util.ArrayList<>());
        }
        if (event.getParticipantIds().size() >= event.getCapacite()) {
            throw new RuntimeException("Event complet");
        }

        if (event.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Déjà inscrit à cet event");
        }

        event.getParticipantIds().add(userId);

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    @Override
    public EventResponseDTO cancelParticipation(String eventId) {
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        if (event.getParticipantIds() != null) {
            event.getParticipantIds().remove(userId);
        }

        return EventMapper.toDto(eventRepository.save(event));
    }
}
