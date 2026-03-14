package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.serviceImpl.SiteCampingService;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SiteCampingController {

    private final SiteCampingService service;

    @GetMapping
    public List<SiteCampingResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public SiteCampingResponse getById(@PathVariable String id) { return service.getById(id); }

    @GetMapping("/disponibles")
    public List<SiteCampingResponse> getDisponibles() { return service.getDisponibles(); }

    @GetMapping("/localisation/{localisation}")
    public List<SiteCampingResponse> getByLocalisation(@PathVariable String localisation) {
        return service.getByLocalisation(localisation);
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    public List<SiteCampingResponse> getByProprietaire(@PathVariable String proprietaireId) {
        return service.getByProprietaire(proprietaireId);
    }

    @GetMapping("/filtrer")
    public List<SiteCampingResponse> filtrer(
            @RequestParam(required = false) Double prixMin,
            @RequestParam(required = false) Double prixMax,
            @RequestParam(required = false) Boolean disponible) {
        return service.filtrer(prixMin, prixMax, disponible);
    }

    @PostMapping
    public SiteCampingResponse create(@RequestBody SiteCampingRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public SiteCampingResponse update(@PathVariable String id,
                                      @RequestBody SiteCampingRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok("Site supprimé avec succès.");
    }
}