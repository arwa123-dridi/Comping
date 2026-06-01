package tn.comping.spring.backendcomping.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.dto.SignupDTO;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private final SignupService signupService;

    public SignupController(SignupService signupService) {
        this.signupService = signupService;
    }

    // Endpoint to register a new user
    @PostMapping("/registerUser")
    public ResponseEntity<?> registerUser(@RequestBody SignupDTO dto) {
        try {
            SignupEntity newUser = signupService.registerUser(dto);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get user by ID (ajouté par Mariem)
    @GetMapping("/getUserById/{id}")
    public SignupEntity getUserById(@PathVariable String id) {
        return signupService.getUserById(id);
    }

    // Get all livreurs (delivery users) (ajouté par Mariem)
    @GetMapping("/livreurs")
    public ResponseEntity<List<SignupEntity>> getLivreurs() {
        return ResponseEntity.ok(signupService.getLivreurs());
    }
}