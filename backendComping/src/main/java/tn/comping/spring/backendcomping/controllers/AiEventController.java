package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.AiRecommendationDTO;
import tn.comping.spring.backendcomping.dto.UserProfileDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.AiEventRecommendationService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiEventController {
    private final AiEventRecommendationService  aiEventRecommendationService;
    // ─────────────────────────────────────────
    // POST /api/ai/recommend-events
    // ─────────────────────────────────────────
    @PostMapping("/recommend-events")
    public ResponseEntity<AiRecommendationDTO> recommendEvents(
            @RequestBody UserProfileDTO userProfile) {

        AiRecommendationDTO result = aiEventRecommendationService
                .recommendEvents(userProfile);

        return ResponseEntity.ok(result);
    }
}
