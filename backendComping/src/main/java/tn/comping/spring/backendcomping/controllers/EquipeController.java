package tn.comping.spring.backendcomping.controllers;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tn.comping.spring.backendcomping.services.serviceImpl.IEquipeService;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> getEquipeById(@PathVariable String id) {
        return ResponseEntity.ok(equipeService.getEquipeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<EquipeResponseDTO> updateEquipe(
            @PathVariable String id,
            @Valid @RequestBody EquipeRequestDTO dto) {
        return ResponseEntity.ok(equipeService.updateEquipe(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<Void> deleteEquipe(@PathVariable String id) {
        equipeService.deleteEquipe(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{equipeId}/membres/{utilisateurId}")
    public ResponseEntity<EquipeResponseDTO> ajouterMembre(
            @PathVariable String equipeId,
            @PathVariable String utilisateurId) {

        return ResponseEntity.ok(
                equipeService.ajouterMembre(equipeId, utilisateurId)
        );
    }

    @DeleteMapping("/{equipeId}/membres/{utilisateurId}")
    public ResponseEntity<Void> retirerMembre(
            @PathVariable String equipeId,
            @PathVariable String utilisateurId) {
        equipeService.retirerMembre(equipeId, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/avec-place")
    public ResponseEntity<List<EquipeResponseDTO>> getEquipesAvecPlace() {
        return ResponseEntity.ok(equipeService.getEquipesAvecPlace());
    }
}