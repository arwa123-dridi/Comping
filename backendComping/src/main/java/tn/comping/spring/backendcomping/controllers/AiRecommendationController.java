package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.EventResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.AiEventRecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AiRecommendationController {

    private final AiEventRecommendationService recommendationService;

    @GetMapping("/events")
    public ResponseEntity<List<EventResponseDTO>> getRecommendedEvents(@RequestParam String userId) {
        return ResponseEntity.ok(recommendationService.getCollaborativeRecommendations(userId));
    }
}
