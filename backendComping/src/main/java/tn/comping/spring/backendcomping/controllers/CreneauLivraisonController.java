package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonRequest;
import tn.comping.spring.backendcomping.dto.CreneauLivraisonResponse;
import tn.comping.spring.backendcomping.services.CreneauLivraisonService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_CRENEAU_LIVRAISON)
public class CreneauLivraisonController {

    private final CreneauLivraisonService service;

    // POST http://localhost:8087/api/creneaux-livraison
    @PostMapping(Constants.CREATE_CRENEAU_LIVRAISON)
    public ResponseEntity<CreneauLivraisonResponse> createCreneau(
            @RequestBody CreneauLivraisonRequest dto) {
        log.info("Creating CreneauLivraison: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCreneau(dto));
    }

    // GET http://localhost:8087/api/creneaux-livraison
    @GetMapping(Constants.GET_ALL_CRENEAUX_LIVRAISON)
    public ResponseEntity<List<CreneauLivraisonResponse>> getAllCreneaux() {
        log.info("Getting all CreneauxLivraison");
        return ResponseEntity.ok(service.getAllCreneaux());
    }

    // GET http://localhost:8087/api/creneaux-livraison/{id}
    @GetMapping(Constants.GET_CRENEAU_LIVRAISON_BY_ID)
    public ResponseEntity<CreneauLivraisonResponse> getCreneauById(
            @PathVariable String id) {
        log.info("Getting CreneauLivraison by id: {}", id);
        return ResponseEntity.ok(service.getCreneauById(id));
    }

    // PUT http://localhost:8087/api/creneaux-livraison/{id}
    @PutMapping(Constants.UPDATE_CRENEAU_LIVRAISON)
    public ResponseEntity<CreneauLivraisonResponse> updateCreneau(
            @PathVariable String id,
            @RequestBody CreneauLivraisonRequest dto) {
        log.info("Updating CreneauLivraison id: {}", id);
        return ResponseEntity.ok(service.updateCreneau(id, dto));
    }

    // DELETE http://localhost:8087/api/creneaux-livraison/{id}
    @DeleteMapping(Constants.DELETE_CRENEAU_LIVRAISON)
    public ResponseEntity<Void> deleteCreneau(@PathVariable String id) {
        log.info("Deleting CreneauLivraison id: {}", id);
        service.deleteCreneau(id);
        return ResponseEntity.noContent().build();
    }
}