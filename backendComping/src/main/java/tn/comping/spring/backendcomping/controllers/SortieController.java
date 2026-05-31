package tn.comping.spring.backendcomping.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.ISortieService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sorties")
@RequiredArgsConstructor

public class SortieController {

    private final ISortieService sortieService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<SortieResponseDTO> createSortie(@Valid @RequestBody SortieRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sortieService.createSortie(dto));
    }

    @GetMapping
    public ResponseEntity<List<SortieResponseDTO>> getAllSorties() {
        return ResponseEntity.ok(sortieService.getAllSorties());
    }

    @GetMapping("/prochaines")
    public ResponseEntity<List<SortieResponseDTO>> getProchainesSorties() {
        return ResponseEntity.ok(sortieService.getProchainesSorties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SortieResponseDTO> getSortieById(@PathVariable String id) {
        return ResponseEntity.ok(sortieService.getSortieById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<SortieResponseDTO> updateSortie(
            @PathVariable String id,
            @Valid @RequestBody SortieRequestDTO dto) {
        return ResponseEntity.ok(sortieService.updateSortie(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANISATEUR')")
    public ResponseEntity<?> deleteSortie(@PathVariable String id) {
        try {
            SortieResponseDTO sortie = sortieService.getSortieById(id);
            if (sortie.getEquipeId() != null) {
                sortieService.dissocierEquipe(id);
            }
            sortieService.deleteSortie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur suppression sortie {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{sortieId}/inscription")
    public ResponseEntity<ParticipationDTO> inscrireParticipant(
            @PathVariable String sortieId,
            @RequestBody tn.comping.spring.backendcomping.dto.InscriptionRequest request) {
        return ResponseEntity.ok(sortieService.inscrireParticipant(sortieId, request));
    }
    @DeleteMapping("/{sortieId}/inscription/{utilisateurId}")
    public ResponseEntity<Void> desinscrireParticipant(
            @PathVariable String sortieId,
            @PathVariable String utilisateurId) {
        sortieService.desinscrireParticipant(sortieId, utilisateurId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sortieId}/participants")
    public ResponseEntity<List<ParticipationDTO>> getParticipantsBySortie(@PathVariable String sortieId) {
        return ResponseEntity.ok(sortieService.getParticipantsBySortie(sortieId));
    }

    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<SortieResponseDTO>> getSortiesByOrganisateur(@PathVariable String organisateurId) {
        return ResponseEntity.ok(sortieService.getSortiesByOrganisateur(organisateurId));
    }
}
