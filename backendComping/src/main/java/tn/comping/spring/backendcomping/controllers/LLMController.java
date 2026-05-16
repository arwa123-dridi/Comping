package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ChatMessageRequest;
import tn.comping.spring.backendcomping.dto.ChatMessageResponse;
import tn.comping.spring.backendcomping.dto.LLMHealthResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.LLMService;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/llm")
@CrossOrigin("*")
public class LLMController {

    private final LLMService llmService;

    /**
     * Main chatbot endpoint - send message and get AI response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatMessageResponse> chat(@RequestBody ChatMessageRequest request) {
        log.info("Chat request received: context={}, messageLength={}", 
                request.getContext(), request.getMessage().length());
        
        ChatMessageResponse response = llmService.chat(request);
        
        return ResponseEntity
                .status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Health check endpoint - verify Ollama is running
     */
    @GetMapping("/health")
    public ResponseEntity<LLMHealthResponse> health() {
        log.info("Health check requested");
        LLMHealthResponse health = llmService.checkHealth();
        
        return ResponseEntity
                .status(health.isAvailable() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(health);
    }

    /**
     * Get emergency guidance for specific emergency type
     */
    @GetMapping("/emergency-guidance")
    public ResponseEntity<ChatMessageResponse> getEmergencyGuidance(
            @RequestParam String type,
            @RequestParam(required = false) String userId) {
        
        log.info("Emergency guidance requested for type: {}", type);
        ChatMessageResponse response = llmService.getEmergencyGuidance(type);
        
        if (userId != null) {
            response.setUserId(userId);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Analyze incident and get suggestions
     */
    @PostMapping("/analyze-incident")
    public ResponseEntity<ChatMessageResponse> analyzeIncident(
            @RequestParam String description,
            @RequestParam(required = false) String userId) {
        
        log.info("Incident analysis requested with descriptionLength={}", description.length());
        ChatMessageResponse response = llmService.analyzeIncident(description);
        
        if (userId != null) {
            response.setUserId(userId);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get incident-related guidance
     */
    @PostMapping("/incident-guidance")
    public ResponseEntity<ChatMessageResponse> getIncidentGuidance(
            @RequestBody ChatMessageRequest request) {
        
        if (request.getContext() == null) {
            request.setContext("incident");
        }
        
        log.info("Incident guidance requested");
        ChatMessageResponse response = llmService.chat(request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get alert-related guidance
     */
    @PostMapping("/alert-guidance")
    public ResponseEntity<ChatMessageResponse> getAlertGuidance(
            @RequestBody ChatMessageRequest request) {
        
        if (request.getContext() == null) {
            request.setContext("alert");
        }
        
        log.info("Alert guidance requested");
        ChatMessageResponse response = llmService.chat(request);
        
        return ResponseEntity.ok(response);
    }
}
