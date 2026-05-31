package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.IncidentRequest;
import tn.comping.spring.backendcomping.dto.IncidentResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.IncidentService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_INCIDENT)
public class IncidentController {

    private final IncidentService service;

    // POST http://localhost:8087/api/incidents
    @PostMapping(Constants.CREATE_INCIDENT)
    public ResponseEntity<IncidentResponse> createIncident(
            @RequestBody IncidentRequest dto) {
        log.info("Creating Incident: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createIncident(dto));
    }

    // GET http://localhost:8087/api/incidents
    @GetMapping(Constants.GET_ALL_INCIDENTS)
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        log.info("Getting all Incidents");
        return ResponseEntity.ok(service.getAllIncidents());
    }

    // GET http://localhost:8087/api/incidents/{id}
    @GetMapping(Constants.GET_INCIDENT_BY_ID)
    public ResponseEntity<IncidentResponse> getIncidentById(
            @PathVariable String id) {
        log.info("Getting Incident by id: {}", id);
        return ResponseEntity.ok(service.getIncidentById(id));
    }

    // PUT http://localhost:8087/api/incidents/{id}
    @PutMapping(Constants.UPDATE_INCIDENT)
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable String id,
            @RequestBody IncidentRequest dto) {
        log.info("Updating Incident id: {}", id);
        return ResponseEntity.ok(service.updateIncident(id, dto));
    }

    // DELETE http://localhost:8087/api/incidents/{id}
    @DeleteMapping(Constants.DELETE_INCIDENT)
    public ResponseEntity<Void> deleteIncident(@PathVariable String id) {
        log.info("Deleting Incident id: {}", id);
        service.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(service.getIncidentsByUserId(userId));
    }
}