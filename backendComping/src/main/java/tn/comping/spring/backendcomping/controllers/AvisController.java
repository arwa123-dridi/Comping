package tn.comping.spring.backendcomping.controllers;

<<<<<<< HEAD
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
=======
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.ReponseAvisRequestDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.services.serviceImpl.AvisService;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AvisController {

    private final AvisService avisService;

    @PostMapping
    public ResponseEntity<AvisResponseDTO> creerAvis(
            @RequestBody AvisRequestDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(avisService.creerAvis(dto, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisResponseDTO> getAvisById(@PathVariable
String id) {
        return ResponseEntity.ok(avisService.getAvisById(id));
    }

    @GetMapping("/cible/{cibleId}")
    public ResponseEntity<List<AvisResponseDTO>> getAvisByCible(
            @PathVariable String cibleId,
            @RequestParam String typeCible) {
        return ResponseEntity.ok(avisService.getAvisByCible(cibleId,
typeCible));
    }

    @GetMapping("/mes-avis")
    public ResponseEntity<List<AvisResponseDTO>>
getMesAvis(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(avisService.getMesAvis(email));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('MODERATEUR') or hasRole('ADMIN')")
    public ResponseEntity<List<AvisResponseDTO>>
getAvisByStatut(@PathVariable StatutAvis statut) {
        return ResponseEntity.ok(avisService.getAvisByStatut(statut));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvisResponseDTO> updateAvis(
            @PathVariable String id,
            @RequestBody AvisRequestDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(avisService.updateAvis(id, dto, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvis(
            @PathVariable String id,
            Authentication authentication) {
        String email = authentication.getName();
        avisService.deleteAvis(id, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize("hasRole('MODERATEUR') or hasRole('ADMIN')")
    public ResponseEntity<AvisResponseDTO> validerAvis(
            @PathVariable String id,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(avisService.validerAvis(id, email));
    }

    @PostMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('MODERATEUR') or hasRole('ADMIN')")
    public ResponseEntity<AvisResponseDTO> rejeterAvis(
            @PathVariable String id,
            @RequestParam String motif,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(avisService.rejeterAvis(id, motif, email));
    }

    @PostMapping("/{avisId}/reponse")
    @PreAuthorize("hasRole('PROPRIETAIRE') or hasRole('BOUTIQUE') or hasRole('ORGANISATEUR') or hasRole('ADMIN')")
    public ResponseEntity<AvisResponseDTO> ajouterReponse(
            @PathVariable String avisId,
            @RequestBody ReponseAvisRequestDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(avisService.ajouterReponse(avisId, dto, email));
    }

    @DeleteMapping("/{avisId}/reponse")
    @PreAuthorize("hasRole('PROPRIETAIRE') or hasRole('BOUTIQUE') or hasRole('ORGANISATEUR') or hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerReponse(
            @PathVariable String avisId,
            Authentication authentication) {
        String email = authentication.getName();
        avisService.supprimerReponse(avisId, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistiques/{cibleId}")
    public ResponseEntity<StatistiquesAvisDTO> getStatistiquesAvis(
            @PathVariable String cibleId,
            @RequestParam String typeCible) {
        return ResponseEntity.ok(avisService.getStatistiquesAvis(cibleId,
typeCible));
    }
}
>>>>>>> origin/mariem-sellami
