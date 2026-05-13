package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.PaiementResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.PaiementService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    // Créer un paiement pour une réservation
    @PostMapping("/create/{reservationId}")
    public ResponseEntity<PaiementResponse> create(
            @PathVariable String reservationId,
            @RequestBody Map<String, Object> body) {
        double montant = Double.parseDouble(body.get("montant").toString());
        String methode = body.getOrDefault("methode", "CARTE").toString();
        return ResponseEntity.ok(paiementService.createPaiement(reservationId, montant, methode));
    }

    // Valider un paiement (après succès Stripe)
    @PostMapping("/valider/{paiementId}")
    public ResponseEntity<PaiementResponse> valider(@PathVariable String paiementId) {
        return ResponseEntity.ok(paiementService.validerPaiement(paiementId));
    }

    // Rembourser un paiement
    @PostMapping("/rembourser/{paiementId}")
    public ResponseEntity<PaiementResponse> rembourser(@PathVariable String paiementId) {
        return ResponseEntity.ok(paiementService.rembourserPaiement(paiementId));
    }

    // Récupérer le paiement d'une réservation
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaiementResponse> getByReservation(@PathVariable String reservationId) {
        return ResponseEntity.ok(paiementService.getByReservationId(reservationId));
    }

    // Tous les paiements
    @GetMapping
    public List<PaiementResponse> getAll() {
        return paiementService.getAll();
    }
}