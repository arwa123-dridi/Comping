package tn.comping.spring.backendcomping.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.comping.spring.backendcomping.dto.LoginDTORequest;
import tn.comping.spring.backendcomping.dto.LoginDTOResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.PasswordResetService;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;
import tn.comping.spring.backendcomping.utils.Constants;

import jakarta.servlet.http.HttpServletRequest;
import tn.comping.spring.backendcomping.config.JwtUtils; 
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequest;
import tn.comping.spring.backendcomping.dto.MessageResponse;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "Opérations d'authentification et réinitialisation mot de passe")
public class AuthController {
    private final SignupService signupService;
    private final PasswordResetService passwordResetService;
    private final JwtUtils jwtUtils;
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTORequest dto) {
        try {
            LoginDTOResponse response = signupService.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

@PostMapping("/forgot-password")
    @Operation(
        summary = "Demander la réinitialisation du mot de passe",
        description = "Envoie un email contenant un lien sécurisé de réinitialisation (token unique, expire dans 1 heure) à l'utilisateur si l'email existe. Email doit être valide et associé à un utilisateur existant."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lien de réinitialisation envoyé avec succès",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Email invalide ou utilisateur non trouvé",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))})
    })
    public ResponseEntity<?> forgotPassword(@RequestBody @jakarta.validation.Valid ForgotPasswordRequest request) {
        try {
            MessageResponse response = passwordResetService.requestPasswordReset(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @jakarta.validation.Valid ResetPasswordRequest request) {
        try {
            MessageResponse response = passwordResetService.resetPassword(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
