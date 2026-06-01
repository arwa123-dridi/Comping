package tn.comping.spring.backendcomping.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.comping.spring.backendcomping.ai.dto.PostDraft;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;
import tn.comping.spring.backendcomping.ai.model.AiSuggestionLog;
import tn.comping.spring.backendcomping.ai.repository.AiSuggestionLogRepository;
import tn.comping.spring.backendcomping.ai.trends.TrendsAggregatorService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiSuggestionServiceTest {

    @Mock
    private TrendsAggregatorService trendsService;

    @Mock
    private AiSuggestionLogRepository logRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AiSuggestionService aiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiService, "groqApiKey", "test-key");
        ReflectionTestUtils.setField(aiService, "groqApiUrl", "https://api.groq.com/openai/v1/chat/completions");
        ReflectionTestUtils.setField(aiService, "groqModel", "llama-3.3-70b-versatile");
        ReflectionTestUtils.setField(aiService, "objectMapper", objectMapper);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.AUTHORIZATION), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("suggestTopics retourne 3 sujets valides depuis Groq")
    void suggestTopics_returnsThreeTopics() throws Exception {
        // Arrange
        List<TrendItem> mockTrends = List.of(
                new TrendItem("reddit", "Best camping spots", "Amazing spots near Paris", 1500),
                new TrendItem("youtube", "Camping gear 2025", "Top gear review", 8),
                new TrendItem("rss", "Eco camping tips", "How to camp responsibly", 3)
        );
        when(trendsService.aggregateTrends()).thenReturn(mockTrends);

        String groqJson = """
                {"topics": [
                  "Les meilleurs spots camping près de Paris cet été",
                  "Équipement camping 2025 : nos incontournables",
                  "Camping éco-responsable : adopter les bons gestes"
                ]}
                """;
        com.fasterxml.jackson.databind.JsonNode groqNode = objectMapper.readTree(
                "{\"choices\":[{\"message\":{\"content\":" + objectMapper.writeValueAsString(groqJson) + "}}]}"
        );
        when(responseSpec.bodyToMono(com.fasterxml.jackson.databind.JsonNode.class))
                .thenReturn(Mono.just(groqNode));

        // Act
        List<String> topics = aiService.suggestTopics();

        // Assert
        assertThat(topics).hasSize(3);
        verify(trendsService).aggregateTrends();
    }

    @Test
    @DisplayName("generateFullPost retourne un PostDraft et persiste un log MongoDB")
    void generateFullPost_returnsAndLogs() throws Exception {
        // Arrange
        String topic = "Les meilleurs spots camping près de Paris";
        String groqJson = """
                {
                  "title": "Paris et ses campings secrets",
                  "content": "À seulement quelques kilomètres de la capitale, des campings paradisiaques vous attendent. Découvrez nos coups de cœur pour une escapade verte sans stress.",
                  "hashtags": ["#camping", "#paris", "#nature"]
                }
                """;
        com.fasterxml.jackson.databind.JsonNode groqNode = objectMapper.readTree(
                "{\"choices\":[{\"message\":{\"content\":" + objectMapper.writeValueAsString(groqJson) + "}}]}"
        );
        when(responseSpec.bodyToMono(com.fasterxml.jackson.databind.JsonNode.class))
                .thenReturn(Mono.just(groqNode));
        when(logRepository.save(any(AiSuggestionLog.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PostDraft draft = aiService.generateFullPost(topic, "user@test.com");

        // Assert
        assertThat(draft).isNotNull();
        assertThat(draft.title()).isNotBlank();
        assertThat(draft.content()).isNotBlank();
        assertThat(draft.hashtags()).isNotEmpty();
        verify(logRepository).save(any(AiSuggestionLog.class));
    }

    @Test
    @DisplayName("suggestTopics utilise le fallback si toutes les sources retournent vide")
    void suggestTopics_usesFallbackTopicsOnEmptyTrends() throws Exception {
        // Arrange — pas de tendances
        when(trendsService.aggregateTrends()).thenReturn(List.of());

        String groqJson = "{\"topics\":[\"Sujet A\",\"Sujet B\",\"Sujet C\"]}";
        com.fasterxml.jackson.databind.JsonNode groqNode = objectMapper.readTree(
                "{\"choices\":[{\"message\":{\"content\":" + objectMapper.writeValueAsString(groqJson) + "}}]}"
        );
        when(responseSpec.bodyToMono(com.fasterxml.jackson.databind.JsonNode.class))
                .thenReturn(Mono.just(groqNode));

        // Act
        List<String> topics = aiService.suggestTopics();

        // Assert — Groq est quand même appelé, on reçoit ses sujets
        assertThat(topics).isNotEmpty();
    }
}
