package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.EquipeScoreDTO;
import tn.comping.spring.backendcomping.dto.SortieScoreDTO;
import tn.comping.spring.backendcomping.entities.UserProfile;
import tn.comping.spring.backendcomping.services.serviceImpl.IRecommandationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommandations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class RecommandationController {

    private final IRecommandationService recommandationService;

    // GET /api/recommandations/sorties?userId=xxx
    @GetMapping("/sorties")
    public ResponseEntity<List<SortieScoreDTO>> getSortiesRecommandees(
            @RequestParam String userId) {
        log.info("GET /recommandations/sorties?userId={}", userId);
        return ResponseEntity.ok(recommandationService.recommanderSorties(userId));
    }

    // GET /api/recommandations/equipes?userId=xxx
    @GetMapping("/equipes")
    public ResponseEntity<List<EquipeScoreDTO>> getEquipesRecommandees(
            @RequestParam String userId) {
        log.info("GET /recommandations/equipes?userId={}", userId);
        return ResponseEntity.ok(recommandationService.recommanderEquipes(userId));
    }

    // ── Profil utilisateur ─────────────────────────────────────────────────
    @GetMapping("/profil")
    public ResponseEntity<UserProfile> getProfil(@RequestParam String userId) {
        return ResponseEntity.ok(
                recommandationService.construireOuMettreAJourProfil(userId));
    }

    // ── Mise à jour profil après inscription ──────────────────────────────
    @PostMapping("/participation/mise-a-jour")
    public ResponseEntity<Map<String, String>> mettreAJourProfil(
            @RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        recommandationService.mettreAJourProfilApresInscription(userId);
        return ResponseEntity.ok(Map.of("message", "Profil mis à jour"));
    }

}
