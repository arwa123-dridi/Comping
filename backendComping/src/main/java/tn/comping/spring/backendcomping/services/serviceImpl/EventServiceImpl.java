package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.utils.mapper.EventMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements  EventService{
    private final EventRepository eventRepository;
    @Override
    public EventResponseDTO createEvent(EventRequestDTO dto) {
        Event event = EventMapper.toEntity(dto);
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
                .map(EventMapper::toDto)
                .collect(Collectors.toList());
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
        return EventMapper.toDto(eventRepository.save(existing));
    }

    @Override
    public void deleteEvent(String id) {
            if (!eventRepository.existsById(id)) {
                throw new RuntimeException("Event not found with id: " + id);
            }
            eventRepository.deleteById(id);

    }
}
