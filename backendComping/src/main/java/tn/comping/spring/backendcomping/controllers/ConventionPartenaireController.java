package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireRequest;
import tn.comping.spring.backendcomping.dto.ConventionPartenaireResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.ConventionPartenaireService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@AllArgsConstructor
@RequestMapping(Constants.BASE_URL_CONVENTION_PARTENAIRE)
public class ConventionPartenaireController {

    private static final Logger log = LoggerFactory.getLogger(ConventionPartenaireController.class);
    private final ConventionPartenaireService service;

    // POST http://localhost:8087/api/conventions-partenaires
    @PostMapping(Constants.CREATE_CONVENTION_PARTENAIRE)
    public ResponseEntity<ConventionPartenaireResponse> createConvention(
            @RequestBody ConventionPartenaireRequest dto) {
        log.info("Creating ConventionPartenaire: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createConvention(dto));
    }

    // GET http://localhost:8087/api/conventions-partenaires
    @GetMapping(Constants.GET_ALL_CONVENTIONS_PARTENAIRES)
    public ResponseEntity<List<ConventionPartenaireResponse>> getAllConventions() {
        log.info("Getting all ConventionsPartenaires");
        return ResponseEntity.ok(service.getAllConventions());
    }

    // GET http://localhost:8087/api/conventions-partenaires/{id}
    @GetMapping(Constants.GET_CONVENTION_PARTENAIRE_BY_ID)
    public ResponseEntity<ConventionPartenaireResponse> getConventionById(
            @PathVariable String id) {
        log.info("Getting ConventionPartenaire by id: {}", id);
        return ResponseEntity.ok(service.getConventionById(id));
    }

    // PUT http://localhost:8087/api/conventions-partenaires/{id}
    @PutMapping(Constants.UPDATE_CONVENTION_PARTENAIRE)
    public ResponseEntity<ConventionPartenaireResponse> updateConvention(
            @PathVariable String id,
            @RequestBody ConventionPartenaireRequest dto) {
        log.info("Updating ConventionPartenaire id: {}", id);
        return ResponseEntity.ok(service.updateConvention(id, dto));
    }

    // DELETE http://localhost:8087/api/conventions-partenaires/{id}
    @DeleteMapping(Constants.DELETE_CONVENTION_PARTENAIRE)
    public ResponseEntity<Void> deleteConvention(@PathVariable String id) {
        log.info("Deleting ConventionPartenaire id: {}", id);
        service.deleteConvention(id);
        return ResponseEntity.noContent().build();
    }
}