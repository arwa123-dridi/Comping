package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tn.comping.spring.backendcomping.dto.UrgenceRequest;
import tn.comping.spring.backendcomping.dto.UrgenceResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.UrgenceService;
import java.util.List;

@RestController
@RequestMapping("/api/urgences")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UrgenceController {
    
    private final UrgenceService service;
    
    @PostMapping
    public ResponseEntity<UrgenceResponse> creer(@Valid @RequestBody UrgenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerUrgence(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UrgenceResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }
    
    @GetMapping
    public ResponseEntity<List<UrgenceResponse>> getAll(
            @RequestParam(required = false) String site,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String niveau) {
        
        if (site != null && !site.isBlank()) {
            return ResponseEntity.ok(service.getBySiteCampingId(site));
        }
        if (statut != null && !statut.isBlank()) {
            return ResponseEntity.ok(service.getByStatut(statut));
        }
        if (niveau != null && !niveau.isBlank()) {
            return ResponseEntity.ok(service.getByNiveauUrgence(niveau));
        }
        return ResponseEntity.ok(service.getAll());
    }
    
    @GetMapping("/site/{siteCampingId}")
    public ResponseEntity<List<UrgenceResponse>> getBySite(@PathVariable String siteCampingId) {
        return ResponseEntity.ok(service.getBySiteCampingId(siteCampingId));
    }
    
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<UrgenceResponse>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.getByStatut(statut));
    }
    
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<UrgenceResponse>> getByNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(service.getByNiveauUrgence(niveau));
    }
    
    @GetMapping("/assignee/{assigneId}")
    public ResponseEntity<List<UrgenceResponse>> getByAssignee(@PathVariable String assigneId) {
        return ResponseEntity.ok(service.getByAssignee(assigneId));
    }
    
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<List<UrgenceResponse>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }
    
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<UrgenceResponse>> getByCategory(@PathVariable String categorie) {
        return ResponseEntity.ok(service.getByCategory(categorie));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UrgenceResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UrgenceRequest request) {
        return ResponseEntity.ok(service.updateUrgence(id, request));
    }
    
    @PatchMapping("/{id}/statut/{statut}")
    public ResponseEntity<UrgenceResponse> updateStatut(
            @PathVariable String id,
            @PathVariable String statut) {
        return ResponseEntity.ok(service.updateStatut(id, statut));
    }
    
    @PatchMapping("/{id}/assigner/{assigneId}")
    public ResponseEntity<UrgenceResponse> assign(
            @PathVariable String id,
            @PathVariable String assigneId) {
        return ResponseEntity.ok(service.assignTo(id, assigneId));
    }
    
    @PatchMapping("/{id}/resoudre")
    public ResponseEntity<UrgenceResponse> resolve(
            @PathVariable String id,
            @RequestParam String resolution) {
        return ResponseEntity.ok(service.resolveUrgence(id, resolution));
    }
    
    @PatchMapping("/{id}/rejeter")
    public ResponseEntity<UrgenceResponse> reject(
            @PathVariable String id,
            @RequestParam String reason) {
        return ResponseEntity.ok(service.rejectUrgence(id, reason));
    }
    
    @PatchMapping("/{id}/completer")
    public ResponseEntity<UrgenceResponse> complete(@PathVariable String id) {
        return ResponseEntity.ok(service.complete(id));
    }
    
    @PatchMapping("/{id}/commentaire")
    public ResponseEntity<UrgenceResponse> addComment(
            @PathVariable String id,
            @RequestParam String comment) {
        return ResponseEntity.ok(service.addComment(id, comment));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteUrgence(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getCount() {
        return ResponseEntity.ok(service.count());
    }
    
    @GetMapping("/stats/count/{statut}")
    public ResponseEntity<Long> getCountByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.countByStatut(statut));
    }
}
