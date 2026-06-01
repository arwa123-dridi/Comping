package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.AlerteService;
import java.util.List;


@RestController
@RequestMapping("/api/alertes")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService service;

    @GetMapping
    public List<AlerteResponse> getAll() { return service.getAllAlertes(); }

    @GetMapping("/site/{siteCampingId}")
    public List<AlerteResponse> getBySite(@PathVariable String siteCampingId) {
        return service.getAlertesBySite(siteCampingId);
    }

    @PostMapping
    public AlerteResponse declencher(@RequestBody AlerteRequest request) {
        return service.declencherAlerte(request);
    }

    @PatchMapping("/{id}/statut")
    public AlerteResponse updateStatut(@PathVariable String id,
                                       @RequestParam String statut) {
        return service.updateStatut(id, statut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.supprimerAlerte(id);
        return ResponseEntity.ok("Alerte supprimée avec succès.");
    }
}