package tn.comping.spring.backendcomping.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.ai.dto.PostDraft;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;
import tn.comping.spring.backendcomping.ai.model.AiSuggestionLog;
import tn.comping.spring.backendcomping.ai.repository.AiSuggestionLogRepository;
import tn.comping.spring.backendcomping.ai.trends.TrendsAggregatorService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service central IA : suggère des sujets de publication basés sur les tendances réelles
 * du camping, puis génère un post complet via Groq (llama-3.3-70b-versatile).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSuggestionService {

    private final TrendsAggregatorService trendsService;
    private final AiSuggestionLogRepository logRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.model}")
    private String groqModel;

    private static final int MAX_TRENDS_IN_PROMPT = 25;

    // ─── Étape 1 ───────────────────────────────────────────────────────────────

    /**
     * Agrège les tendances camping et demande à Groq 3 sujets de publication.
     */
    public List<String> suggestTopics() {
        List<TrendItem> trends = trendsService.aggregateTrends();
        String context = buildTrendsContext(trends);

        String prompt = """
                Tu es un expert en marketing de contenu camping.
                Voici les tendances RÉELLES actuelles dans le monde du camping :

                %s

                En te basant UNIQUEMENT sur ces tendances, propose 3 sujets de publication
                accrocheurs pour un réseau social camping.
                Réponds STRICTEMENT au format JSON, sans aucun texte autour :
                {"topics": ["sujet 1", "sujet 2", "sujet 3"]}

                Les sujets doivent être en français, courts (max 80 caractères), engageants.
                """.formatted(context);

        String raw = callGroq(prompt);
        return parseTopics(raw);
    }

    // ─── Étape 2 ───────────────────────────────────────────────────────────────

    /**
     * Génère un post complet (titre + contenu + hashtags) pour un sujet donné
     * et persiste un log dans MongoDB.
     *
     * @param topic  sujet sélectionné par l'utilisateur
     * @param userId email de l'utilisateur connecté (peut être null)
     */
    public PostDraft generateFullPost(String topic, String userId) {
        String prompt = """
                Rédige une publication complète pour un réseau social camping sur le sujet :
                "%s"

                Réponds STRICTEMENT en JSON :
                {
                  "title": "Titre accrocheur (max 60 caractères)",
                  "content": "Texte de 150-200 mots, ton chaleureux, en français",
                  "hashtags": ["#camping", "#...", "..."]
                }
                """.formatted(topic);

        String raw = callGroq(prompt);
        PostDraft draft = parseDraft(raw, topic);

        persistLog(topic, draft, userId);
        return draft;
    }

    // ─── Appel Groq ────────────────────────────────────────────────────────────

    private String callGroq(String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", groqModel,
                "messages", List.of(Map.of("role", "user", "content", userPrompt)),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.8
        );

        log.info("Appel Groq API (modèle {})", groqModel);
        JsonNode response = webClientBuilder.build()
                .post()
                .uri(groqApiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) throw new RuntimeException("Groq API: réponse vide");
        return response.path("choices").path(0).path("message").path("content").asText("");
    }

    // ─── Parsers ───────────────────────────────────────────────────────────────

    private List<String> parseTopics(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            JsonNode topics = node.path("topics");
            if (topics.isArray()) {
                List<String> result = new java.util.ArrayList<>();
                topics.forEach(t -> result.add(t.asText()));
                return result;
            }
        } catch (Exception e) {
            log.warn("Parsing JSON topics échoué, tentative regex — {}", e.getMessage());
        }
        // Fallback regex : extrait les chaînes entre guillemets dans un tableau JSON
        return extractStringsWithRegex(raw);
    }

    private PostDraft parseDraft(String raw, String fallbackTopic) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            String title = node.path("title").asText(fallbackTopic);
            String content = node.path("content").asText("");
            List<String> hashtags = new java.util.ArrayList<>();
            node.path("hashtags").forEach(h -> hashtags.add(h.asText()));
            return new PostDraft(title, content, hashtags);
        } catch (Exception e) {
            log.warn("Parsing JSON draft échoué — {}", e.getMessage());
            return new PostDraft(fallbackTopic, raw, List.of("#camping"));
        }
    }

    private List<String> extractStringsWithRegex(String raw) {
        Pattern p = Pattern.compile("\"([^\"]{5,80})\"");
        Matcher m = p.matcher(raw);
        List<String> found = new java.util.ArrayList<>();
        while (m.find() && found.size() < 3) {
            String candidate = m.group(1);
            if (!candidate.startsWith("topics") && !candidate.startsWith("sujet")) {
                found.add(candidate);
            }
        }
        if (found.isEmpty()) {
            found.addAll(List.of(
                    "Les meilleures destinations camping de la saison",
                    "Astuces incontournables pour votre prochain bivouac",
                    "Équipement camping : nos coups de cœur du moment"
            ));
        }
        return found;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String buildTrendsContext(List<TrendItem> trends) {
        return trends.stream()
                .limit(MAX_TRENDS_IN_PROMPT)
                .map(t -> "- [%s] %s — %s".formatted(
                        t.source(),
                        t.title(),
                        t.description().isBlank() ? "(pas de description)" : t.description()
                ))
                .collect(Collectors.joining("\n"));
    }

    private void persistLog(String topic, PostDraft draft, String userId) {
        try {
            AiSuggestionLog log = AiSuggestionLog.builder()
                    .topic(topic)
                    .draft(draft)
                    .timestamp(Instant.now())
                    .userId(userId)
                    .build();
            logRepository.save(log);
        } catch (Exception e) {
            AiSuggestionService.log.warn("Impossible de persister le log AI — {}", e.getMessage());
        }
    }
}
