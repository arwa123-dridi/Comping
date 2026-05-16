package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
=======
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
>>>>>>> origin/ahmed
import tn.comping.spring.backendcomping.config.JwtUtils;
import tn.comping.spring.backendcomping.config.SecurityUtils;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
<<<<<<< HEAD
import tn.comping.spring.backendcomping.entities.StatutEvent;
=======
>>>>>>> origin/ahmed
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.utils.mapper.ActivityMapper;
import tn.comping.spring.backendcomping.utils.mapper.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements  EventService{
    private final EventRepository eventRepository;
    private final ActivityRepository activityRepository;
    private final SecurityUtils securityUtils;
<<<<<<< HEAD
    private  final CarteFideliteService carteFideliteService;
=======
>>>>>>> origin/ahmed
    @Override
    public EventResponseDTO createEvent(EventRequestDTO dto) {
        String userId = securityUtils.getCurrentUserId();
        Event event = EventMapper.toEntity(dto);
        event.setOrganisateurId(userId);
<<<<<<< HEAD
        event.setStatut(StatutEvent.EN_ATTENTE);
=======
>>>>>>> origin/ahmed
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
<<<<<<< HEAD
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Event introuvable"));
=======
        // 1. Récupérer user connecté
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));
>>>>>>> origin/ahmed

        if (event.getParticipantIds() == null) {
            event.setParticipantIds(new java.util.ArrayList<>());
        }
<<<<<<< HEAD

        if (event.getParticipantIds().size() >= event.getCapacite()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event complet");
        }

        if (event.getParticipantIds().contains(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà inscrit à cet event");
=======
        if (event.getParticipantIds().size() >= event.getCapacite()) {
            throw new RuntimeException("Event complet");
        }

        if (event.getParticipantIds().contains(userId)) {
            throw new RuntimeException("Déjà inscrit à cet event");
>>>>>>> origin/ahmed
        }

        event.getParticipantIds().add(userId);

        Event saved = eventRepository.save(event);
<<<<<<< HEAD

=======
>>>>>>> origin/ahmed
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
<<<<<<< HEAD

    @Override
    public EventResponseDTO validerEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        event.setStatut(StatutEvent.VALIDE);

        return EventMapper.toDto(eventRepository.save(event));
    }

    @Override
    public long countEvents() {
        return eventRepository.count();
    }

    @Override
    public EventResponseDTO rejectEvent(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event introuvable"));

        event.setStatut(StatutEvent.REJETE);

        return EventMapper.toDto(eventRepository.save(event));
    }
=======
>>>>>>> origin/ahmed
}
