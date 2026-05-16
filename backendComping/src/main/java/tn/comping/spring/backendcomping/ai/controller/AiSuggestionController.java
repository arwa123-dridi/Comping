package tn.comping.spring.backendcomping.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.ai.dto.PostDraft;
import tn.comping.spring.backendcomping.ai.dto.TopicRequest;
import tn.comping.spring.backendcomping.ai.service.AiSuggestionService;

import java.util.List;

/**
 * Endpoints REST pour la fonctionnalité de suggestion IA de publications camping.
 *
 * GET  /api/ai/suggestions/topics    → 3 sujets basés sur les tendances réelles
 * POST /api/ai/suggestions/generate  → post complet (titre + contenu + hashtags)
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/suggestions")
@RequiredArgsConstructor
public class AiSuggestionController {

    private final AiSuggestionService aiService;

    /**
     * Étape 1 : retourne 3 sujets de publication accrocheurs basés sur les tendances camping.
     */
    @GetMapping("/topics")
    public ResponseEntity<List<String>> getSuggestedTopics() {
        log.info("Requête de suggestion de sujets IA");
        List<String> topics = aiService.suggestTopics();
        return ResponseEntity.ok(topics);
    }

    /**
     * Étape 2 : génère un post complet pour le sujet sélectionné par l'utilisateur.
     */
    @PostMapping("/generate")
    public ResponseEntity<PostDraft> generatePost(
            @RequestBody TopicRequest request,
            Authentication authentication) {
        log.info("Génération d'un post IA pour le sujet : {}", request.topic());
        String userId = authentication != null ? authentication.getName() : null;
        PostDraft draft = aiService.generateFullPost(request.topic(), userId);
        return ResponseEntity.ok(draft);
    }
}
