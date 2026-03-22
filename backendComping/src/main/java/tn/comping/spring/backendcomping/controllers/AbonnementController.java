package tn.comping.spring.backendcomping.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.services.AbonnementService;

import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnements", description = "PHASE 4 - Suivi utilisateurs")
public class AbonnementController {

    private final AbonnementService abonnementService;

    @PostMapping("/suivre/{userId}")
    @Operation(summary = "Suivre utilisateur")
    public ResponseEntity<AbonnementResponseDTO> suivre(@PathVariable String userId, 
                                                       Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(abonnementService.suivre(userId, email));
    }

    @DeleteMapping("/ne-plus-suivre/{userId}")
    @Operation(summary = "Ne plus suivre")
    public ResponseEntity<Void> nePlusSuivre(@PathVariable String userId, 
                                            Authentication authentication) {
        String email = authentication.getName();
        abonnementService.nePlusSuivre(userId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mes-abonnements")
    @Operation(summary = "Qui je suis")
    public ResponseEntity<List<AbonnementResponseDTO>> mesAbonnements(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(abonnementService.getMesAbonnements(email));
    }

    @GetMapping("/mes-abonnes")
    @Operation(summary = "Mes abonnés")
    public ResponseEntity<List<AbonnementResponseDTO>> mesAbonnes(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(abonnementService.getMesAbonnes(email));
    }

    @GetMapping("/stats")
    @Operation(summary = "Stats abonnements")
    public ResponseEntity<Object> stats(@RequestParam String userId) {
        return ResponseEntity.ok(abonnementService.getStats(userId));
    }
}

