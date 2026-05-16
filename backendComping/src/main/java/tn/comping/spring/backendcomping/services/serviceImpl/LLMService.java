package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.comping.spring.backendcomping.dto.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    @Value("${ollama.url:http://127.0.0.1:11434/api/generate}")
    private String ollamaUrl;

    @Value("${ollama.model:gpt-oss-20b}")
    private String modelName;

    @Value("${ollama.temperature:0}")
    private int temperature;

    @Value("${ollama.top-k:40}")
    private int topK;

    @Value("${ollama.top-p:0.9}")
    private double topP;

    @Value("${ollama.num-predict:256}")
    private int numPredict;

    @Value("${ollama.context-length:4096}")
    private int contextLength;

    @Value("${ollama.verbose-logging:false}")
    private boolean verboseLogging;

    private final RestTemplate restTemplate;

    /**
     * Build context-aware prompt for incident/emergency questions
     */
    private String buildContextualPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tu es un assistant intelligent de gestion des urgences et des incidents pour un site de camping en Tunisie. ");
        prompt.append("Tu fournis des conseils clairs et exploitables STRICTEMENT liés au camping, aux situations d'urgence, au signalement d'incidents, à la sécurité, aux alertes et aux procédures de sécurité.\n\n");
        prompt.append("INSTRUCTION CRITIQUE : Tu DOIS répondre UNIQUEMENT en FRANÇAIS. ");
        prompt.append("Si l'utilisateur pose une question NON LIÉE au camping, aux urgences, aux incidents, à la sécurité, aux alertes ou au site de camping en Tunisie, tu DOIS refuser poliment de répondre et déclarer que tu es un assistant d'urgence/camping. Ne réponds pas aux questions de culture générale.\n\n");
        prompt.append("Formatte ta réponse en texte brut avec des puces simples (- ou *). N'utilise PAS de gras (**), d'italique ou de formatage markdown, car l'interface de chat ne le supporte pas.\n\n");

        switch (context != null ? context.toLowerCase() : "") {
            case "incident":
                prompt.append("Contexte : L'utilisateur signale ou pose une question sur un INCIDENT (accident, dommage, panne d'équipement).\n");
                prompt.append("Fournis des conseils sur les procédures de signalement, l'évaluation de la gravité et les actions immédiates.\n\n");
                break;
            case "alert":
                prompt.append("Contexte : L'utilisateur fait face à une ALERTE ACTIVE ou une situation d'urgence.\n");
                prompt.append("Fournis des conseils urgents et concis en donnant la priorité à la sécurité. Sois direct et concret.\n\n");
                break;
            case "emergency":
                prompt.append("Contexte : L'utilisateur gère une situation d'URGENCE nécessitant une réponse immédiate.\n");
                prompt.append("Donne la priorité aux procédures de sécurité et d'escalade. Fournis uniquement des conseils critiques.\n\n");
                break;
            default:
                prompt.append("Contexte : L'utilisateur pose des questions générales sur la gestion des urgences/incidents ou le camping en Tunisie.\n\n");
        }

        prompt.append("Procédures d'urgence du site de camping en Tunisie :\n");
        prompt.append("- Pour les urgences médicales vitales : Appelez immédiatement le SAMU (190) ou la Protection Civile (198).\n");
        prompt.append("- Pour les urgences de police ou de sécurité : Appelez la Police (197) ou la Garde Nationale (193).\n");
        prompt.append("- Pour les incidents : Documentez les détails (quoi, quand, qui, lieu) et signalez-les à la direction du site.\n");
        prompt.append("- Pour les pannes d'équipement : Isolez la zone et informez l'équipe de maintenance.\n");
        prompt.append("- Pour les blessures : Prodiguez les premiers soins, puis demandez une assistance médicale.\n\n");

        prompt.append("Question de l'utilisateur : ").append(userMessage).append("\n\n");
        prompt.append("Fournis une réponse claire, utile et sûre en FRANÇAIS. Garde la réponse concise (moins de 200 mots).\n");
        prompt.append("GPU-Accelerated Response (RTX 5070 - 8GB VRAM available).");

        return prompt.toString();
    }

    /**
     * Send message to Ollama and get response
     */
    public ChatMessageResponse chat(ChatMessageRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String contextualPrompt = buildContextualPrompt(request.getMessage(), request.getContext());

            OllamaRequest ollamaRequest = OllamaRequest.builder()
                    .model(modelName)
                    .prompt(contextualPrompt)
                    .stream(false)
                    .temperature(temperature)  // From application.properties
                    .top_k(topK)                // From application.properties
                    .top_p(topP)                // From application.properties
                    .num_predict(numPredict)    // From application.properties
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OllamaRequest> entity = new HttpEntity<>(ollamaRequest, headers);

            if (verboseLogging) {
                log.info("GPU-Accelerated Request: model={}, context={}, promptLength={}, temperature={}, topK={}", 
                        modelName, request.getContext(), contextualPrompt.length(), temperature, topK);
            }

            ResponseEntity<OllamaResponse> response = restTemplate.postForEntity(
                    ollamaUrl,
                    entity,
                    OllamaResponse.class
            );

            long processingTime = System.currentTimeMillis() - startTime;
            if (verboseLogging) {
                log.info("GPU Response received in {}ms (Model processing time: {}ms)", 
                        processingTime, response.getBody().getEvalDuration());
            }

            return ChatMessageResponse.builder()
                    .message(request.getMessage())
                    .response(response.getBody().getResponse().trim())
                    .context(request.getContext())
                    .userId(request.getUserId())
                    .timestamp(new java.util.Date())
                    .processingTimeMs(processingTime)
                    .success(true)
                    .build();

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Error communicating with Ollama at {}: {}", ollamaUrl, e.getMessage(), e);

            return ChatMessageResponse.builder()
                    .message(request.getMessage())
                    .response("Je m'excuse, mais je suis actuellement indisponible. Veuillez réessayer plus tard.")
                    .context(request.getContext())
                    .userId(request.getUserId())
                    .timestamp(new java.util.Date())
                    .processingTimeMs(processingTime)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Check if Ollama service is available
     */
    public LLMHealthResponse checkHealth() {
        long startTime = System.currentTimeMillis();

        try {
            OllamaRequest healthCheck = OllamaRequest.builder()
                    .model(modelName)
                    .prompt("Bonjour")
                    .stream(false)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OllamaRequest> entity = new HttpEntity<>(healthCheck, headers);

            restTemplate.postForEntity(
                    ollamaUrl,
                    entity,
                    OllamaResponse.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            return LLMHealthResponse.builder()
                    .available(true)
                    .modelName(modelName)
                    .status("Ready")
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Health check failed: {}", e.getMessage());

            return LLMHealthResponse.builder()
                    .available(false)
                    .modelName(modelName)
                    .status("Unavailable")
                    .responseTimeMs(responseTime)
                    .build();
        }
    }

    /**
     * Analyze incident description and suggest category/severity
     */
    public ChatMessageResponse analyzeIncident(String description) {
        String prompt = "Analyse cette description d'incident et suggère : 1) Catégorie, 2) Niveau de gravité, 3) Actions recommandées.\n\n" +
                "Description : " + description;

        return chat(ChatMessageRequest.builder()
                .message(prompt)
                .context("incident")
                .build());
    }

    /**
     * Get emergency response guidance
     */
    public ChatMessageResponse getEmergencyGuidance(String emergencyType) {
        String prompt = "Quelles sont les procédures étape par étape pour gérer une urgence de type " + emergencyType + " sur un site de camping ?";

        return chat(ChatMessageRequest.builder()
                .message(prompt)
                .context("emergency")
                .build());
    }
}
