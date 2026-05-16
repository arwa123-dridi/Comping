package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;

import jakarta.servlet.http.HttpServletRequest;
import tn.comping.spring.backendcomping.config.JwtUtils; 
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost", "http://127.0.0.1"})
public class AuthController {
    private  final SignupService signupService;
    private final JwtUtils jwtUtils;
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTORequest dto) {
        try {
            LoginDTOResponse response = signupService.login(dto);
            return ResponseEntity.ok(Map.of("token", response.getToken()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        if (!jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Token invalide ou déjà expiré"));
        }

        jwtUtils.blacklistToken(token);
        return ResponseEntity.ok(Map.of("message", "Déconnecté avec succès"));
    }

    return ResponseEntity.badRequest().body(Map.of("error", "Token manquant"));
}

}
