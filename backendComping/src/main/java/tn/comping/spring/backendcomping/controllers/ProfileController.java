package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
@CrossOrigin("*")
public class ProfileController {

    private final IProfileService profileService;
    private final SignupService signupService;

    @GetMapping("/{userId}")
    public ResponseEntity<SignupEntity> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<SignupEntity> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(profileService.updateProfile(userId, dto));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<String> updatePassword(
            @PathVariable String userId,
            @Valid @RequestBody UpdatePasswordDTO dto) {
        return ResponseEntity.ok(profileService.updatePassword(userId, dto));
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<String> updateAvatar(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.updateAvatar(userId, file));
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<SignupEntity> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(profileService.getUserByEmail(email));
    }

    @GetMapping(Constants.GET_ALL_USERS)
    public ResponseEntity<List<SignupEntity>> getAllUsers() {
        return ResponseEntity.ok(profileService.getAllUsers());
    }

    @DeleteMapping(Constants.DELETE_USER)
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        profileService.deleteUser(userId);
        return ResponseEntity.ok("Utilisateur supprimé");
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SignupEntity> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        boolean statut = body.get("statut");
        SignupEntity updatedUser = profileService.updateStatus(id, statut);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/count")
    public long getTotalUsers() {
        return signupService.getTotalUsers();
    }
}
