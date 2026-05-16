package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;
import tn.comping.spring.backendcomping.services.PasswordResetService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO dto) {
        try {
            String result = passwordResetService.requestPasswordReset(dto);
            log.info("Forgot password request for: {}", dto.getEmail());
            return ResponseEntity.ok(Map.of("message", "Email de réinitialisation envoyé. Vérifiez votre boîte de réception."));
        } catch (RuntimeException e) {
            log.warn("Forgot password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO dto) {
        try {
            String result = passwordResetService.resetPassword(dto);
            log.info("Password reset successful for token: {}", dto.getToken());
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
        } catch (RuntimeException e) {
            log.warn("Reset password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
