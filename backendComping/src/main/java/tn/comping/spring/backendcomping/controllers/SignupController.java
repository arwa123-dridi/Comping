package tn.comping.spring.backendcomping.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.dto.SignupDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@RequestBody SignupDTO dto) {
        try {
            SignupEntity newUser = signupService.registerUser(dto);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            signupService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("message", "Compte vérifié avec succès."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("getUserById/{id}")
    public SignupEntity getUserById(@PathVariable String id) {
        return signupService.getUserById(id);
    }

    @GetMapping("/livreurs")
    public ResponseEntity<List<SignupEntity>> getLivreurs() {
        return ResponseEntity.ok(signupService.getLivreurs());
    }
}
