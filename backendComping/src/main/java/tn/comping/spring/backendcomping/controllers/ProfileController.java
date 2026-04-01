package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.dto.UpdateProfileDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.IProfileService;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final IProfileService profileService;

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

    @PutMapping("/{userId}/photo")
    public ResponseEntity<String> updatePhoto(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        String photoUrl = request.get("photo");  // ← Récupère la photo du body
        return ResponseEntity.ok(profileService.updatePhoto(userId, photoUrl));
    }
    @GetMapping("/by-email/{email}")
    public ResponseEntity<SignupEntity> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(profileService.getUserByEmail(email));
    }
}