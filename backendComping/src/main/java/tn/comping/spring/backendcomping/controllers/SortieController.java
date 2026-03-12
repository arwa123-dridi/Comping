package tn.comping.spring.backendcomping.controllers;

import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.services.interfaces.ISortieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sorties")
@RequiredArgsConstructor
public class SortieController {

    private final ISortieService sortieService;

    @PostMapping
    public ResponseEntity<SortieResponseDTO> createSortie(@Valid @RequestBody SortieRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sortieService.createSortie(dto));
    }

    @GetMapping
    public ResponseEntity<List<SortieResponseDTO>> getAllSorties() {
        return ResponseEntity.ok(sortieService.getAllSorties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SortieResponseDTO> getSortieById(@PathVariable String id) {
        return ResponseEntity.ok(sortieService.getSortieById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SortieResponseDTO> updateSortie(
            @PathVariable String id,
            @Valid @RequestBody SortieRequestDTO dto) {
        return ResponseEntity.ok(sortieService.updateSortie(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSortie(@PathVariable String id) {
        sortieService.deleteSortie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sortieId}/inscription")
    public ResponseEntity<ParticipationDTO> inscrireParticipant(
            @PathVariable String sortieId,
            @RequestParam String utilisateurId,
            @RequestParam String utilisateurNom,
            @RequestParam String utilisateurEmail) {
        return ResponseEntity.ok(sortieService.inscrireParticipant(
                sortieId, utilisateurId, utilisateurNom, utilisateurEmail));
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

    @GetMapping("/prochaines")
    public ResponseEntity<List<SortieResponseDTO>> getProchainesSorties() {
        return ResponseEntity.ok(sortieService.getProchainesSorties());
    }
}