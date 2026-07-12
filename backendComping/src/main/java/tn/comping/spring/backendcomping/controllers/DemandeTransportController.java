package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.CreneauSuggestionResponse;
import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.dto.TraitementDemandeRequest;
import tn.comping.spring.backendcomping.services.serviceImpl.CreneauSuggestionService;
import tn.comping.spring.backendcomping.services.serviceImpl.DemandeTransportService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_DEMANDE_TRANSPORT)
@PreAuthorize("isAuthenticated()")
public class DemandeTransportController {

    private final DemandeTransportService demandeTransportService;
    private final CreneauSuggestionService creneauSuggestionService;

    @PostMapping(Constants.CREATE_DEMANDE_TRANSPORT)
    public ResponseEntity<DemandeTransportResponse> createDemandeTransport(
            @RequestBody DemandeTransportRequest dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandeTransportService.createDemandeTransport(dto, authentication.getName()));
    }

    @GetMapping(Constants.GET_MES_DEMANDES_TRANSPORT)
    public ResponseEntity<List<DemandeTransportResponse>> getMesDemandes(Authentication authentication) {
        return ResponseEntity.ok(demandeTransportService.getMesDemandes(authentication.getName()));
    }

    @GetMapping(Constants.GET_DEMANDE_TRANSPORT_BY_ID)
    public ResponseEntity<DemandeTransportResponse> getDemandeTransportById(
            @PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(demandeTransportService.getDemandeTransportById(id, authentication.getName()));
    }

    @GetMapping(Constants.GET_ALL_DEMANDES_TRANSPORT)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<List<DemandeTransportResponse>> getAllDemandesTransport() {
        return ResponseEntity.ok(demandeTransportService.getAllDemandesTransport());
    }

    @PutMapping(Constants.UPDATE_DEMANDE_TRANSPORT)
    public ResponseEntity<DemandeTransportResponse> updateDemandeTransport(@PathVariable String id,
                                                                              @RequestBody DemandeTransportRequest dto,
                                                                              Authentication authentication) {
        return ResponseEntity.ok(demandeTransportService.updateDemandeTransport(id, dto, authentication.getName()));
    }

    @DeleteMapping(Constants.DELETE_DEMANDE_TRANSPORT)
    public ResponseEntity<Void> deleteDemandeTransport(@PathVariable String id, Authentication authentication) {
        demandeTransportService.deleteDemandeTransport(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(Constants.TRAITER_DEMANDE_TRANSPORT)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<DemandeTransportResponse> traiterDemande(@PathVariable String id,
                                                                     @RequestBody TraitementDemandeRequest dto,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(demandeTransportService.traiterDemande(id, dto, authentication.getName()));
    }

    @GetMapping(Constants.SUGGESTION_CRENEAU_DEMANDE_TRANSPORT)
    @PreAuthorize("hasAnyRole('ORGANISATEUR','ADMIN')")
    public ResponseEntity<CreneauSuggestionResponse> suggestionCreneau(@PathVariable String id) {
        return ResponseEntity.ok(creneauSuggestionService.suggest(id));
    }
}
