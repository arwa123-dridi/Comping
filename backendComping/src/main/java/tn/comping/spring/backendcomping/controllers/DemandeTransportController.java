package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.DemandeTransportRequest;
import tn.comping.spring.backendcomping.dto.DemandeTransportResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.DemandeTransportService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(Constants.BASE_URL_DEMANDE_TRANSPORT)
public class DemandeTransportController {

    private final DemandeTransportService demandeTransportService;

    @PostMapping(Constants.CREATE_DEMANDE_TRANSPORT)
    public ResponseEntity<DemandeTransportResponse> createDemandeTransport(@RequestBody DemandeTransportRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandeTransportService.createDemandeTransport(dto));
    }

    @GetMapping(Constants.GET_DEMANDE_TRANSPORT_BY_ID)
    public ResponseEntity<DemandeTransportResponse> getDemandeTransportById(@PathVariable String id) {
        return ResponseEntity.ok(demandeTransportService.getDemandeTransportById(id));
    }

    @GetMapping(Constants.GET_ALL_DEMANDES_TRANSPORT)
    public ResponseEntity<List<DemandeTransportResponse>> getAllDemandesTransport() {
        return ResponseEntity.ok(demandeTransportService.getAllDemandesTransport());
    }

    @PutMapping(Constants.UPDATE_DEMANDE_TRANSPORT)
    public ResponseEntity<DemandeTransportResponse> updateDemandeTransport(@PathVariable String id,
                                                                              @RequestBody DemandeTransportRequest dto) {
        return ResponseEntity.ok(demandeTransportService.updateDemandeTransport(id, dto));
    }

    @DeleteMapping(Constants.DELETE_DEMANDE_TRANSPORT)
    public ResponseEntity<Void> deleteDemandeTransport(@PathVariable String id) {
        demandeTransportService.deleteDemandeTransport(id);
        return ResponseEntity.noContent().build();
    }
}