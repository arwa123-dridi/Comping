package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.config.SecurityUtils;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.IProfileService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;
    private final SecurityUtils   securityUtils;

    // ─────────────────────────────────────────────────────────────
    //  Helper : l'utilisateur courant peut-il agir sur cet userId ?
    //  ✅ Oui si c'est lui-même OU s'il est ADMIN
    // ─────────────────────────────────────────────────────────────
    private boolean canActOn(String userId) {
        String currentId   = securityUtils.getCurrentUserId();
        String currentRole = securityUtils.getCurrentUserRole();
        return currentId.equals(userId) || "ADMIN".equals(currentRole);
    }

    // ── GET profil ────────────────────────────────────────────────
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SignupEntity> getProfile(@PathVariable String userId) {
        if (!canActOn(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez consulter que votre propre profil");
        }
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    // ── PUT profil ────────────────────────────────────────────────
    // ✅ CORRIGÉ : endpoint était /profile mais frontend appelle PUT /{userId}
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SignupEntity> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileDTO dto) {
        if (!canActOn(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez modifier que votre propre profil");
        }
        return ResponseEntity.ok(profileService.updateProfile(userId, dto));
    }

    // ── Alias /profile pour compatibilité ─────────────────────────
    @PutMapping("/{userId}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SignupEntity> updateProfileAlias(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileDTO dto) {
        return updateProfile(userId, dto);
    }

    // ── PUT mot de passe ──────────────────────────────────────────
    // ✅ CORRIGÉ : admin peut aussi changer son propre mot de passe
    @PutMapping("/{userId}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updatePassword(
            @PathVariable String userId,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        if (!canActOn(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez changer que votre propre mot de passe");
        }
        return ResponseEntity.ok(profileService.updatePassword(userId, dto));
    }

    // ── PUT photo ─────────────────────────────────────────────────
    // ✅ CORRIGÉ : admin peut changer sa propre photo (403 résolu)
    @PutMapping("/{userId}/photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updatePhoto(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        if (!canActOn(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez modifier que votre propre photo");
        }
        String photoUrl = request.get("photo");
        if (photoUrl == null || photoUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo manquante");
        }
        return ResponseEntity.ok(profileService.updatePhoto(userId, photoUrl));
    }

    // ── ADMIN uniquement ──────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SignupEntity>> getAllUsers() {
        return ResponseEntity.ok(profileService.getAllUsers());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        profileService.deleteUser(userId);
        return ResponseEntity.ok("Utilisateur supprimé");
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignupEntity> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        boolean statut = Boolean.TRUE.equals(body.get("statut"));
        return ResponseEntity.ok(profileService.updateStatus(id, statut));
    }
}
