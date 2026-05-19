package tn.comping.spring.backendcomping.services.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public void streamChat(String message, List<Map<String, String>> history, SseEmitter emitter) {
        String systemPrompt = "You are a safety-focused camping assistant. Prioritize first aid, emergency protocols, and conservative advice.";
        
        StringBuilder fullPrompt = new StringBuilder(systemPrompt + "\n\n");
        for (Map<String, String> entry : history) {
            fullPrompt.append(entry.get("role")).append(": ").append(entry.get("content")).append("\n");
        }
        fullPrompt.append("User: ").append(message).append("\nAssistant: ");

        Map<String, Object> request = Map.of(
            "model", "llama3",
            "prompt", fullPrompt.toString(),
            "stream", true
        );

        webClientBuilder.build()
                .post()
                .uri(OLLAMA_URL)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .subscribe(
                    chunk -> {
                        try {
                            JsonNode node = objectMapper.readTree(chunk);
                            String text = node.get("response").asText();
                            boolean done = node.get("done").asBoolean();
                            
                            emitter.send(SseEmitter.event().data(text));
                            
                            if (done) {
                                emitter.complete();
                            }
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        log.error("Ollama streaming error: {}", error.getMessage());
                        emitter.completeWithError(error);
                    }
                );
    }

    public tn.comping.spring.backendcomping.dto.ChatMessageResponse analyzeIncident(String description) {
        String prompt = "Analyze this camping incident and provide a severity assessment (LOW, MEDIUM, HIGH, CRITICAL) and a suggested resolution: " + description;
        
        Map<String, Object> request = Map.of(
            "model", "llama3",
            "prompt", prompt,
            "stream", false,
            "format", "json"
        );

        try {
            String responseStr = webClientBuilder.build()
                    .post()
                    .uri(OLLAMA_URL)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseStr);
            String aiResponse = root.get("response").asText();
            
            return tn.comping.spring.backendcomping.dto.ChatMessageResponse.builder()
                    .response(aiResponse)
                    .build();
        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage());
            return tn.comping.spring.backendcomping.dto.ChatMessageResponse.builder()
                    .response("{\"severity\": \"MEDIUM\", \"resolution\": \"Contact manual support.\"}")
                    .build();
        }
    }
}
