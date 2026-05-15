package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;

import java.util.List;

public interface EventService {

    EventResponseDTO createEvent(EventRequestDTO dto);
    EventResponseDTO getEventById(String id);
    List<EventResponseDTO> getAllEvents();
    EventResponseDTO updateEvent(String id, EventRequestDTO dto);
    void deleteEvent(String id);
    long countByStatut(String statut);
    EventResponseDTO participate(String eventId);
    EventResponseDTO cancelParticipation(String eventId);
    long countEvents();
}
