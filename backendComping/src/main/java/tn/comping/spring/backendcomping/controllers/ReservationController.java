package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Reservation;
import tn.comping.spring.backendcomping.entities.StatutReservation;
import tn.comping.spring.backendcomping.services.serviceImpl.ReservationService;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @GetMapping
    public List<ReservationResponse> getAll() {
        return service.getAllReservations();
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable String id) {
        return service.getReservationById(id);
    }

    @GetMapping("/historique/{utilisateurId}")
    public List<ReservationResponse> getHistorique(@PathVariable String utilisateurId) {
        return service.getHistoriqueUtilisateur(utilisateurId);
    }

    @PostMapping
    public ReservationResponse create(@RequestBody ReservationRequest request) {
        return service.createReservation(request);
    }

    @PatchMapping("/{id}/statut")
    public ReservationResponse updateStatut(@PathVariable String id,
                                            @RequestParam StatutReservation statut) {
        return service.updateStatut(id, statut);
    }

    @PatchMapping("/{id}")
public ResponseEntity<Reservation> update(@PathVariable String id,
                                          @RequestBody Reservation request) {
    Reservation updated = service.updateReservation(id, request);
    return ResponseEntity.ok(updated);
}

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.deleteReservation(id);
        return ResponseEntity.ok("Réservation supprimée avec succès.");
    }
}