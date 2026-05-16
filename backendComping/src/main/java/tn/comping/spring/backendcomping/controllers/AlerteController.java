package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.serviceImpl.AlerteService;
import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService service;

    @GetMapping
    public List<AlerteResponse> getAll(@RequestParam(required = false) String siteCampingId,
                                       @RequestParam(required = false) String statut) {
        return service.getAlertesByCritere(siteCampingId, statut);
    }

    @GetMapping("/site/{siteCampingId}")
    public List<AlerteResponse> getBySite(@PathVariable String siteCampingId) {
        return service.getAlertesBySite(siteCampingId);
    }

    @GetMapping("/actives")
    public List<AlerteResponse> getActives() {
        return service.getAlertesByCritere(null, "ACTIVE");
    }

    @PostMapping
    public AlerteResponse declencher(@Valid @RequestBody AlerteRequest request) {
        return service.declencherAlerte(request);
    }

    @PatchMapping("/{id}/statut")
    public AlerteResponse updateStatut(@PathVariable String id,
                                       @RequestParam String statut) {
        return service.updateStatut(id, statut);
    }

    @PatchMapping("/{id}/prendre-en-charge")
    public AlerteResponse prendreEnCharge(@PathVariable String id) {
        return service.prendreEnCharge(id);
    }

    @PatchMapping("/{id}/cloturer")
    public AlerteResponse cloturer(@PathVariable String id) {
        return service.cloturer(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.supprimerAlerte(id);
        return ResponseEntity.ok("Alerte supprimée avec succès.");
    }
}