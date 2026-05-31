package tn.comping.spring.backendcomping.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;
import tn.comping.spring.backendcomping.services.serviceImpl.ISortieService;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sorties")
@RequiredArgsConstructor
public class SortieController {

    private final ISortieService     sortieService;
    private final WeatherService     weatherService;
    private final AIChecklistService aiChecklistService;

    @PostMapping
    @PreAuthorize("hasRole('ORGANISATEUR')")
    public ResponseEntity<SortieResponseDTO> createSortie(@Valid @RequestBody SortieRequestDTO dto) {

        SortieResponseDTO sortie = sortieService.createSortie(dto);

        String villeMeteo = dto.getLieuArrivee() != null ? dto.getLieuArrivee() : dto.getLieuDepart();

        if (villeMeteo != null && dto.getDateDebut() != null) {
            try {
                // ✅ UNE SEULE déclaration LocalDate — pas de String intermédiaire
                LocalDate date = dto.getDateDebut().toLocalDate();
                int diff       = convertDifficulte(dto.getDifficulte().name());

                log.info("Checklist auto pour {} le {} difficulte {}", villeMeteo, date, diff);

                WeatherDTO weather = weatherService.getWeather(villeMeteo, date);

                ChecklistRequest req = new ChecklistRequest();
                req.setTemperature(weather.getTemperature());
                req.setPrecipitation(weather.getPrecipitation());
                req.setWind_speed(weather.getWindSpeed());
                req.setHumidity(weather.getHumidity());
                req.setDifficulte(diff);

                ChecklistResponse checklist = aiChecklistService.predictChecklist(req);

                if (checklist != null && checklist.isSuccess()) {
                    sortie.setChecklistRecommandee(checklist.getChecklistItem());
                    log.info("Checklist auto generee: {}", checklist.getChecklistItem());
                }

            } catch (Exception e) {
                log.warn("Checklist auto non generee ({}): {}", villeMeteo, e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(sortie);
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
            @RequestBody InscriptionRequest request) {
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
    public ResponseEntity<List<ParticipationDTO>> getParticipantsBySortie(
            @PathVariable String sortieId) {
        return ResponseEntity.ok(sortieService.getParticipantsBySortie(sortieId));
    }

    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<SortieResponseDTO>> getSortiesByOrganisateur(
            @PathVariable String organisateurId) {
        return ResponseEntity.ok(sortieService.getSortiesByOrganisateur(organisateurId));
    }

    private int convertDifficulte(String d) {
        if (d == null) return 2;
        return switch (d.toUpperCase()) {
            case "FACILE"    -> 2;
            case "MOYEN"     -> 3;
            case "DIFFICILE" -> 4;
            default          -> 2;
        };
    }
}