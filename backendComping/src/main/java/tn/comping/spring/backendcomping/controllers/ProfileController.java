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
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.utils.Constants;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;
    private final SecurityUtils   securityUtils;
    private final SignupService   signupService; // utilisé pour getTotalUsers

    // Helper : l'utilisateur courant peut-il agir sur cet userId ?
    // ✅ Oui si c'est lui-même OU s'il est ADMIN
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

    // ── GET by email (ajouté par Mariem) ─────────────────────────
    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignupEntity> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(profileService.getUserByEmail(email));
    }

    // ── PUT profil (alias /profile) ───────────────────────────────
    @PutMapping("/{userId}/profile")
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

    // ── PUT mot de passe ──────────────────────────────────────────
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

    // ── ADMIN : liste de tous les utilisateurs ────────────────────
    @GetMapping(Constants.GET_ALL_USERS)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SignupEntity>> getAllUsers() {
        return ResponseEntity.ok(profileService.getAllUsers());
    }

    // ── ADMIN : suppression d'un utilisateur ──────────────────────
    @DeleteMapping(Constants.DELETE_USER)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        profileService.deleteUser(userId);
        return ResponseEntity.ok("Utilisateur supprimé");
    }

    // ── ADMIN : activation/désactivation d'un compte ──────────────
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignupEntity> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        boolean statut = Boolean.TRUE.equals(body.get("statut"));
        SignupEntity updatedUser = profileService.updateStatus(id, statut);
        return ResponseEntity.ok(updatedUser);
    }

    // ── ADMIN : nombre total d'utilisateurs (ajouté par Mariem) ───
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public long getTotalUsers() {
        return signupService.getTotalUsers();
    }
}