package tn.comping.spring.backendcomping.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.dto.AiRecommendationDTO;
import tn.comping.spring.backendcomping.dto.EventAiDTO;
import tn.comping.spring.backendcomping.dto.UserProfileDTO;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.repositories.EventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @Service
@RequiredArgsConstructor
public class AiEventRecommendationService {
    private final EventRepository eventRepository;
    private final WebClient.Builder webClientBuilder;

    private static final String PYTHON_API_URL = "http://127.0.0.1:5000";
    // ─────────────────────────────────────────
    // Méthode principale de recommandation
    // ─────────────────────────────────────────
    public AiRecommendationDTO recommendEvents(UserProfileDTO userProfile) {

        // 1. Récupérer tous les events depuis MongoDB
        List<Event> events = eventRepository.findAll();

        // 2. Convertir les events en EventAiDTO
        List<EventAiDTO> eventAiDTOs = events.stream()
                .map(this::convertToAiDTO)
                .collect(Collectors.toList());

        // 3. Construire le body de la requête vers Python
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user", userProfile);
        requestBody.put("events", eventAiDTOs);
        // 4. Appeler l'API Python
        AiRecommendationDTO result = webClientBuilder
                .build()
                .post()
                .uri(PYTHON_API_URL + "/api/recommend")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiRecommendationDTO.class)
                .block();

        return result;
    }
    // ─────────────────────────────────────────
    // Convertir Event → EventAiDTO
    // ─────────────────────────────────────────
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
