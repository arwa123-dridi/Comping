package tn.comping.spring.backendcomping.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.IEquipeService;

import java.util.List;
@RestController
@RequestMapping("/api/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final IEquipeService equipeService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<EquipeResponseDTO> createEquipe(@Valid @RequestBody EquipeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipeService.createEquipe(dto));
    }

    @GetMapping
    public ResponseEntity<List<EquipeResponseDTO>> getAllEquipes() {
        return ResponseEntity.ok(equipeService.getAllEquipes());
    }

    @GetMapping("/avec-place")
    public ResponseEntity<List<EquipeResponseDTO>> getEquipesAvecPlace() {
        return ResponseEntity.ok(equipeService.getEquipesAvecPlace());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> getEquipeById(@PathVariable String id) {
        return ResponseEntity.ok(equipeService.getEquipeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATEUR', 'ADMIN')")
    public ResponseEntity<EquipeResponseDTO> updateEquipe(
            @PathVariable String id,
            @Valid @RequestBody EquipeRequestDTO dto) {
        return ResponseEntity.ok(equipeService.updateEquipe(id, dto));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANISATEUR', 'ADMIN')")
    public ResponseEntity<Void> deleteEquipe(@PathVariable String id) {
        equipeService.deleteEquipe(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{equipeId}/membres/{utilisateurId}")
    public ResponseEntity<EquipeResponseDTO> ajouterMembre(
            @PathVariable String equipeId,
            @PathVariable String utilisateurId,
            @RequestBody java.util.Map<String, String> body) {

        String utilisateurNom = body.get("utilisateurNom");
        return ResponseEntity.ok(
                equipeService.ajouterMembre(equipeId, utilisateurId, utilisateurNom)
        );
    }

    @DeleteMapping("/{equipeId}/membres/{utilisateurId}")
    public ResponseEntity<Void> retirerMembre(
            @PathVariable String equipeId,
            @PathVariable String utilisateurId) {
        equipeService.retirerMembre(equipeId, utilisateurId);
        return ResponseEntity.noContent().build();
    }
}
