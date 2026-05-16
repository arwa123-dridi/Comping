package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.services.AbonnementService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AbonnementController {

    private final AbonnementService abonnementService;

    /** Suivre un campeur */
    @PostMapping("/suivre")
    public ResponseEntity<AbonnementResponseDTO> suivre(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String suiviId = body.get("suiviId");
        return ResponseEntity.ok(abonnementService.suivre(auth.getName(), suiviId));
    }

    /** Retirer un campeur suivi */
    @DeleteMapping("/retirer/{suiviId}")
    public ResponseEntity<Void> retirer(
            @PathVariable String suiviId,
            Authentication auth) {
        abonnementService.retirer(auth.getName(), suiviId);
        return ResponseEntity.noContent().build();
    }

    /** Liste des campeurs suivis */
    @GetMapping("/mes-abonnements")
    public ResponseEntity<List<AbonnementResponseDTO>> mesAbonnements(Authentication auth) {
        return ResponseEntity.ok(abonnementService.getMesAbonnements(auth.getName()));
    }

    /** Vérifier si on suit un campeur */
    @GetMapping("/est-suivi/{suiviId}")
    public ResponseEntity<Map<String, Boolean>> estSuivi(
            @PathVariable String suiviId,
            Authentication auth) {
        boolean suivi = abonnementService.estSuivi(auth.getName(), suiviId);
        return ResponseEntity.ok(Map.of("suivi", suivi));
    }

    /** Statistiques abonnés/abonnements d'un utilisateur */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Long>> followStats(@PathVariable String userId) {
        return ResponseEntity.ok(abonnementService.getFollowStats(userId));
    }
}
