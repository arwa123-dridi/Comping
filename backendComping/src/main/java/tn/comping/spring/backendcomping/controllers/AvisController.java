package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.serviceImpl.AvisService;
import java.util.List;

@RestController
@RequestMapping("/api/avis")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService service;

    @GetMapping("/site/{siteCampingId}")
    public List<AvisResponse> getBySite(@PathVariable String siteCampingId) {
        return service.getAvisBySite(siteCampingId);
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public List<AvisResponse> getByUtilisateur(@PathVariable String utilisateurId) {
        return service.getAvisByUtilisateur(utilisateurId);
    }

    @PostMapping
    public AvisResponse ajouter(@RequestBody AvisRequest request) {
        return service.ajouterAvis(request);
    }

    @PatchMapping("/{id}/moderer")
    public AvisResponse moderer(@PathVariable String id,
                                @RequestParam String statut) {
        return service.modererAvis(id, statut);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.supprimerAvis(id);
        return ResponseEntity.ok("Avis supprimé avec succès.");
    }
}