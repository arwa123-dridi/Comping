package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import tn.comping.spring.backendcomping.config.SecurityUtils;
import tn.comping.spring.backendcomping.dto.EventRequestDTO;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.StatutEvent;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.CarteFideliteService;
import tn.comping.spring.backendcomping.services.serviceImpl.EventServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CarteFideliteService carteFideliteService; // ✅ AJOUT : injecté dans EventServiceImpl

    @InjectMocks
    private EventServiceImpl eventService;

    private Event eventSample;
    private EventRequestDTO requestSample;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(securityUtils.getCurrentUserId()).thenReturn("org-01");
        when(activityRepository.findAllById(any())).thenReturn(List.of());

        eventSample = Event.builder()
                .idEvent("1")
                .titre("Hackathon Comping")
                .description("Compétition de programmation")
                .prix(0.0)
                .capacite(100)
                .dateDebut(LocalDateTime.of(2025, 6, 1, 9, 0))
                .dateFin(LocalDateTime.of(2025, 6, 2, 18, 0))
                .statut(StatutEvent.EN_ATTENTE)
                .lieu("Tunis")
                .organisateurId("org-01")
                .categorie("Tech")
                .tags(List.of("informatique", "competition"))
                .niveauDifficulte("Avancé")
                .trancheAge("18-35")
                .latitude(36.8065)
                .longitude(10.1815)
                .saison("Été")
                .dureeEnHeures(33)
                .pointsRecompense(100)
                .activityIds(List.of())
                .build();

        requestSample = new EventRequestDTO();
        requestSample.setTitre("Hackathon Comping");
        requestSample.setDescription("Compétition de programmation");
        requestSample.setPrix(0.0);
        requestSample.setCapacite(100);
        requestSample.setLieu("Tunis");
        requestSample.setCategorie("Tech");
    }

    @Test
    void testCreateEvent() {
        when(eventRepository.save(any(Event.class)))
                .thenReturn(eventSample);

        EventResponseDTO response = eventService.createEvent(requestSample);

        assertNotNull(response);
        assertEquals("Hackathon Comping", response.getTitre());
        assertEquals("Tunis", response.getLieu());
        assertEquals("Tech", response.getCategorie());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testGetAllEvents() {
        Event event2 = Event.builder()
                .idEvent("2")
                .titre("Workshop Spring Boot")
                .lieu("Sfax")
                .activityIds(List.of())
                .build();

        when(eventRepository.findAll())
                .thenReturn(List.of(eventSample, event2));

        List<EventResponseDTO> responses = eventService.getAllEvents();

        assertEquals(2, responses.size());
        assertEquals("Hackathon Comping", responses.get(0).getTitre());
        assertEquals("Workshop Spring Boot", responses.get(1).getTitre());

        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById("1"))
                .thenReturn(Optional.of(eventSample));

        EventResponseDTO response = eventService.getEventById("1");

        assertNotNull(response);
        assertEquals("1", response.getIdEvent());
        assertEquals("Hackathon Comping", response.getTitre());
        assertEquals(StatutEvent.EN_ATTENTE, response.getStatut());
    }

    @Test
    void testGetEventById_NotFound() {
        when(eventRepository.findById("999"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.getEventById("999"));
    }

    @Test
    void testUpdateEvent() {
        EventRequestDTO updateRequest = new EventRequestDTO();
        updateRequest.setTitre("Hackathon Comping 2025 — Édition spéciale");
        updateRequest.setCapacite(150);
        updateRequest.setLieu("Tunis");

        when(eventRepository.findById("1"))
                .thenReturn(Optional.of(eventSample));
        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EventResponseDTO response = eventService.updateEvent("1", updateRequest);

        assertNotNull(response);
        assertEquals("Hackathon Comping 2025 — Édition spéciale", response.getTitre());
        assertEquals(150, response.getCapacite());

        verify(eventRepository).findById("1");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testDeleteEvent() {
        // ✅ CORRECTION : deleteEvent() utilise existsById(), pas findById()
        when(eventRepository.existsById("1")).thenReturn(true);
        doNothing().when(eventRepository).deleteById("1");

        eventService.deleteEvent("1");

        verify(eventRepository, times(1)).existsById("1");
        verify(eventRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteEvent_NotFound() {
        // ✅ BONUS : teste le cas où l'event n'existe pas
        when(eventRepository.existsById("999")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> eventService.deleteEvent("999"));

        verify(eventRepository, times(1)).existsById("999");
        verify(eventRepository, never()).deleteById(any());
    }
}