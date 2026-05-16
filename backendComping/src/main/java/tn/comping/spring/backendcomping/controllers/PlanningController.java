package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.SortiePlanifieeDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.IPlanningService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final IPlanningService planningService;

    /**
     * GET /api/planning/{userId}
     * Retourne le planning complet (8 sorties sur 3 mois)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<SortiePlanifieeDTO>> getPlanning(
            @PathVariable String userId) {
        return ResponseEntity.ok(planningService.genererPlanning(userId));
    }

    /**
     * GET /api/planning/{userId}/calendrier?mois=2026-06
     * Sorties recommandées pour un mois donné
     */
    @GetMapping("/{userId}/calendrier")
    public ResponseEntity<List<SortiePlanifieeDTO>> getPlanningParMois(
            @PathVariable String userId,
            @RequestParam String mois) {
        return ResponseEntity.ok(planningService.getPlanningParMois(userId, mois));
    }

    /**
     * POST /api/planning/{userId}/valider
     * Body: { "sortieId": "xxx" }
     * Inscrit l'utilisateur à la sortie choisie
     */
    @PostMapping("/{userId}/valider")
    public ResponseEntity<Map<String, String>> validerSortie(
            @PathVariable String userId,
            @RequestBody Map<String, String> body) {
        String sortieId = body.get("sortieId");
        if (sortieId == null || sortieId.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "sortieId requis"));

        planningService.validerSortie(userId, sortieId);
        return ResponseEntity.ok(Map.of("message", "Inscription confirmée avec succès"));
    }
}