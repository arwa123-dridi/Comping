package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import tn.comping.spring.backendcomping.dto.SecuriteRequest;
import tn.comping.spring.backendcomping.dto.SecuriteResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.SecuriteService;
import java.util.List;

@RestController
@RequestMapping("/api/securite")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SecuriteController {
    
    private final SecuriteService service;
    
    @PostMapping
    public ResponseEntity<SecuriteResponse> creer(@Valid @RequestBody SecuriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerMesure(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SecuriteResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getById(id));
    }
    
    @GetMapping
    public ResponseEntity<List<SecuriteResponse>> getAll(
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
            return ResponseEntity.ok(service.getByNiveauSecurite(niveau));
        }
        return ResponseEntity.ok(service.getAll());
    }
    
    @GetMapping("/site/{siteCampingId}")
    public ResponseEntity<List<SecuriteResponse>> getBySite(@PathVariable String siteCampingId) {
        return ResponseEntity.ok(service.getBySiteCampingId(siteCampingId));
    }
    
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<SecuriteResponse>> getByStatut(@PathVariable String statut) {
        return ResponseEntity.ok(service.getByStatut(statut));
    }
    
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<SecuriteResponse>> getByNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(service.getByNiveauSecurite(niveau));
    }
    
    @GetMapping("/risque/{risque}")
    public ResponseEntity<List<SecuriteResponse>> getByRiskLevel(@PathVariable String risque) {
        return ResponseEntity.ok(service.getByRiskLevel(risque));
    }
    
    @GetMapping("/responsable/{responsableId}")
    public ResponseEntity<List<SecuriteResponse>> getByResponsable(@PathVariable String responsableId) {
        return ResponseEntity.ok(service.getByResponsable(responsableId));
    }
    
    @GetMapping("/haut-risque")
    public ResponseEntity<List<SecuriteResponse>> getHighRisk() {
        return ResponseEntity.ok(service.getHighRiskMeasures());
    }
    
    @GetMapping("/securite-faible/{threshold}")
    public ResponseEntity<List<SecuriteResponse>> getLowSecurity(@PathVariable Integer threshold) {
        return ResponseEntity.ok(service.getLowSecurityScoreMeasures(threshold));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SecuriteResponse> update(
            @PathVariable String id,
            @Valid @RequestBody SecuriteRequest request) {
        return ResponseEntity.ok(service.updateMesure(id, request));
    }
    
    @PatchMapping("/{id}/statut/{statut}")
    public ResponseEntity<SecuriteResponse> updateStatut(
            @PathVariable String id,
            @PathVariable String statut) {
        return ResponseEntity.ok(service.updateStatut(id, statut));
    }
    
    @PatchMapping("/{id}/equipe/{memberId}")
    public ResponseEntity<SecuriteResponse> assignTeamMember(
            @PathVariable String id,
            @PathVariable String memberId) {
        return ResponseEntity.ok(service.assignTeamMember(id, memberId));
    }
    
    @DeleteMapping("/{id}/equipe/{memberId}")
    public ResponseEntity<SecuriteResponse> removeTeamMember(
            @PathVariable String id,
            @PathVariable String memberId) {
        return ResponseEntity.ok(service.removeTeamMember(id, memberId));
    }
    
    @PatchMapping("/{id}/constat")
    public ResponseEntity<SecuriteResponse> recordFinding(
            @PathVariable String id,
            @RequestParam String finding) {
        return ResponseEntity.ok(service.recordFinding(id, finding));
    }
    
    @PatchMapping("/{id}/recommandation")
    public ResponseEntity<SecuriteResponse> addRecommendation(
            @PathVariable String id,
            @RequestParam String recommendation) {
        return ResponseEntity.ok(service.addRecommendation(id, recommendation));
    }
    
    @PatchMapping("/{id}/incident/{incidentId}")
    public ResponseEntity<SecuriteResponse> recordIncident(
            @PathVariable String id,
            @PathVariable String incidentId) {
        return ResponseEntity.ok(service.recordIncident(id, incidentId));
    }
    
    @PatchMapping("/{id}/monitoring-complete")
    public ResponseEntity<SecuriteResponse> completeMonitoring(@PathVariable String id) {
        return ResponseEntity.ok(service.completeMonitoring(id));
    }
    
    @PatchMapping("/{id}/budget")
    public ResponseEntity<SecuriteResponse> updateBudget(
            @PathVariable String id,
            @RequestParam Double amount) {
        return ResponseEntity.ok(service.updateBudgetUsed(id, amount));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteMesure(id);
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
