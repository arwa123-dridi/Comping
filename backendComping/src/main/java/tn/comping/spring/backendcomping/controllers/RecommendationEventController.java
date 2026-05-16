package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.repositories.EventRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.RecomendationActivityService;

import java.util.List;

@RestController
@RequestMapping("/api/recommendationActivity")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RecommendationEventController {
    private final RecomendationActivityService recomendationActivityService;
    private final EventRepository eventRepository;
    @PostMapping("/suggest-activities")
    public List<Activity> suggestActivities(
            @RequestBody Event event) {

        return recomendationActivityService
                .suggestActivities(event);
    }
}
