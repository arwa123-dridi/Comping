package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.dto.TraitementIncidentRequest;
import tn.comping.spring.backendcomping.services.serviceImpl.IncidentService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_INCIDENT)
@PreAuthorize("isAuthenticated()")
public class IncidentController {

    private final IncidentService service;

    @PostMapping(Constants.CREATE_INCIDENT)
    public ResponseEntity<IncidentResponse> createIncident(
            @RequestBody IncidentRequest dto, Authentication authentication) {
        log.info("Creating Incident: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createIncident(dto, authentication.getName()));
    }

    @GetMapping(Constants.GET_MES_INCIDENTS)
    public ResponseEntity<List<IncidentResponse>> getMesIncidents(Authentication authentication) {
        return ResponseEntity.ok(service.getMesIncidents(authentication.getName()));
    }

    @GetMapping(Constants.GET_INCIDENT_BY_ID)
    public ResponseEntity<IncidentResponse> getIncidentById(
            @PathVariable String id, Authentication authentication) {
        log.info("Getting Incident by id: {}", id);
        return ResponseEntity.ok(service.getIncidentById(id, authentication.getName()));
    }

    @GetMapping(Constants.GET_ALL_INCIDENTS)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        log.info("Getting all Incidents");
        return ResponseEntity.ok(service.getAllIncidents());
    }

    @PutMapping(Constants.UPDATE_INCIDENT)
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable String id,
            @RequestBody IncidentRequest dto,
            Authentication authentication) {
        log.info("Updating Incident id: {}", id);
        return ResponseEntity.ok(service.updateIncident(id, dto, authentication.getName()));
    }

    @DeleteMapping(Constants.DELETE_INCIDENT)
    public ResponseEntity<Void> deleteIncident(@PathVariable String id, Authentication authentication) {
        log.info("Deleting Incident id: {}", id);
        service.deleteIncident(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(Constants.TRAITER_INCIDENT)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<IncidentResponse> traiterIncident(@PathVariable String id,
                                                              @RequestBody TraitementIncidentRequest dto,
                                                              Authentication authentication) {
        return ResponseEntity.ok(service.traiterIncident(id, dto, authentication.getName()));
    }

    @PostMapping(Constants.RECLASSIFIER_INCIDENT)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<IncidentResponse> reclassifier(@PathVariable String id) {
        return ResponseEntity.ok(service.reclassifier(id));
    }
}
