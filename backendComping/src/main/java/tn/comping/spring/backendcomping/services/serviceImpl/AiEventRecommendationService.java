package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.dto.AiRecommendationDTO;
import tn.comping.spring.backendcomping.dto.EventAiDTO;
import tn.comping.spring.backendcomping.dto.UserProfileDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.utils.mapper.EventMapper;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiEventRecommendationService {
    private final EventRepository eventRepository;
    private final InteractionRepository interactionRepository;
    private final WebClient.Builder webClientBuilder;

    private static final String PYTHON_API_URL = "http://127.0.0.1:5000";

    public AiRecommendationDTO recommendEvents(UserProfileDTO userProfile) {
        List<Event> events = eventRepository.findAll();
        List<EventAiDTO> eventAiDTOs = events.stream()
                .map(this::convertToAiDTO)
                .collect(Collectors.toList());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user", userProfile);
        requestBody.put("events", eventAiDTOs);

        return webClientBuilder
                .build()
                .post()
                .uri(PYTHON_API_URL + "/api/recommend")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiRecommendationDTO.class)
                .block();
    }

    public List<EventResponseDTO> getCollaborativeRecommendations(String userId) {
        // 1. Find events the current user joined
        List<Interaction> userJoins = interactionRepository.findByAuteurIdAndType(userId, "JOIN_EVENT");
        Set<String> joinedEventIds = userJoins.stream()
                .map(Interaction::getCibleId)
                .collect(Collectors.toSet());

        if (joinedEventIds.isEmpty()) {
            // Fallback: recommend popular events or upcoming events
            return eventRepository.findTop5ByOrderByDateDebutDesc()
                    .stream()
                    .map(EventMapper::toDto)
                    .toList();
        }

        // 2. Find other users who joined at least one of these events
        List<Interaction> otherUsersInteractions = interactionRepository.findByCibleIdInAndTypeAndAuteurIdNot(
                new ArrayList<>(joinedEventIds), "JOIN_EVENT", userId);
        
        Set<String> similarUserIds = otherUsersInteractions.stream()
                .map(Interaction::getAuteurId)
                .collect(Collectors.toSet());

        if (similarUserIds.isEmpty()) {
            return eventRepository.findTop5ByOrderByDateDebutDesc()
                    .stream()
                    .map(EventMapper::toDto)
                    .toList();
        }

        // 3. Find events joined by these similar users that the current user hasn't joined
        List<Interaction> recommendations = interactionRepository.findByAuteurIdInAndTypeAndCibleIdNotIn(
                new ArrayList<>(similarUserIds), "JOIN_EVENT", new ArrayList<>(joinedEventIds));

        Map<String, Long> eventScores = recommendations.stream()
                .collect(Collectors.groupingBy(Interaction::getCibleId, Collectors.counting()));

        List<String> recommendedEventIds = eventScores.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        return eventRepository.findAllById(recommendedEventIds)
                .stream()
                .map(EventMapper::toDto)
                .toList();
    }

    private EventAiDTO convertToAiDTO(Event event) {
        return EventAiDTO.builder()
                .idEvent(event.getIdEvent())
                .titre(event.getTitre())
                .description(event.getDescription())
                .prix(event.getPrix())
                .lieu(event.getLieu())
                .categorie(event.getCategorie())
                .tags(event.getTags())
                .niveauDifficulte(event.getNiveauDifficulte())
                .trancheAge(event.getTrancheAge())
                .saison(event.getSaison())
                .dureeEnHeures(event.getDureeEnHeures())
                .dateDebut(event.getDateDebut())
                .dateFin(event.getDateFin())
                .build();
    }
}
