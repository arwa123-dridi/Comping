package tn.comping.spring.backendcomping.services.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.dto.ChecklistResponseDTO;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AIChecklistService {

    private static final Logger log = LoggerFactory.getLogger(AIChecklistService.class);
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public ChecklistResponseDTO generateChecklist(String destination, int duration, String difficulty, String season) {
        String prompt = String.format(
            "Generate a comprehensive camping gear checklist for a trip to %s for %d days. " +
            "Difficulty: %s, Season: %s. " +
            "Return ONLY a JSON object with categories as keys and arrays of items as values. " +
            "Categories should include: Equipment, Clothing, Food & Water, First Aid, Personal Care.",
            destination, duration, difficulty, season
        );

        Map<String, Object> request = Map.of(
            "model", "llama3", // or any available model
            "prompt", prompt,
            "stream", false,
            "format", "json"
        );

        try {
            String response = webClientBuilder.build()
                    .post()
                    .uri(OLLAMA_URL)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String jsonContent = root.get("response").asText();
            JsonNode categoriesNode = objectMapper.readTree(jsonContent);

            Map<String, List<String>> categories = new HashMap<>();
            categoriesNode.fields().forEachRemaining(entry -> {
                List<String> items = new ArrayList<>();
                if (entry.getValue().isArray()) {
                    entry.getValue().forEach(item -> items.add(item.asText()));
                }
                categories.put(entry.getKey(), items);
            });

            return ChecklistResponseDTO.builder()
                    .destination(destination)
                    .durationDays(duration)
                    .difficulty(difficulty)
                    .season(season)
                    .categories(categories)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate checklist via Ollama: {}", e.getMessage());
            // Fallback to a basic list
            return getFallbackChecklist(destination, duration, difficulty, season);
        }
    }

    private ChecklistResponseDTO getFallbackChecklist(String dest, int dur, String diff, String seas) {
        Map<String, List<String>> fallback = new HashMap<>();
        fallback.put("Essential", List.of("Tent", "Sleeping bag", "Flashlight", "Water bottle"));
        return ChecklistResponseDTO.builder()
                .destination(dest).durationDays(dur).difficulty(diff).season(seas)
                .categories(fallback).build();
    }
}
