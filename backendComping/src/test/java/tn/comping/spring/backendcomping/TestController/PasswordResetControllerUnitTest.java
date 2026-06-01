package tn.comping.spring.backendcomping.TestController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.comping.spring.backendcomping.controllers.PasswordResetController;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;
import tn.comping.spring.backendcomping.services.PasswordResetService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires (Mockito pur) - PasswordResetController")
class PasswordResetControllerUnitTest {

    @Mock private PasswordResetService passwordResetService;

    @InjectMocks private PasswordResetController passwordResetController;

    // =========================================================
    // POST /api/auth/forgot-password
    // =========================================================

    @Test
    @DisplayName("forgotPassword : email valide → 200 OK avec message générique")
    void forgotPassword_emailValide_retourne200AvecMessage() {
        when(passwordResetService.requestPasswordReset(any(ForgotPasswordRequestDTO.class)))
                .thenReturn("Email envoyé avec token: uuid-test");

        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("user@test.com").build();

        ResponseEntity<?> response = passwordResetController.forgotPassword(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsKey("message");
        assertThat(body.get("message")).contains("réinitialisation");
        verify(passwordResetService).requestPasswordReset(dto);
    }

    @Test
    @DisplayName("forgotPassword : service appelle requestPasswordReset une seule fois")
    void forgotPassword_appelleServiceUneSeuleFois() {
        when(passwordResetService.requestPasswordReset(any())).thenReturn("OK");

        passwordResetController.forgotPassword(
                ForgotPasswordRequestDTO.builder().email("user@test.com").build());

        verify(passwordResetService, times(1)).requestPasswordReset(any());
    }

    @Test
    @DisplayName("forgotPassword : email inexistant → 400 BAD_REQUEST avec message erreur")
    void forgotPassword_emailInexistant_retourne400AvecErreur() {
        when(passwordResetService.requestPasswordReset(any()))
                .thenThrow(new RuntimeException("Utilisateur non trouvé"));

        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("inconnu@test.com").build();

        ResponseEntity<?> response = passwordResetController.forgotPassword(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Utilisateur non trouvé");
    }

    @Test
    @DisplayName("forgotPassword : toute RuntimeException → 400 BAD_REQUEST")
    void forgotPassword_runtimeException_retourne400() {
        when(passwordResetService.requestPasswordReset(any()))
                .thenThrow(new RuntimeException("Erreur serveur"));

        ResponseEntity<?> response = passwordResetController.forgotPassword(
                ForgotPasswordRequestDTO.builder().email("any@test.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("forgotPassword : le message de succès est générique (ne révèle pas si email existe)")
    void forgotPassword_succes_retourneMessageGenerique() {
        when(passwordResetService.requestPasswordReset(any())).thenReturn("Token créé");

        ResponseEntity<?> response = passwordResetController.forgotPassword(
                ForgotPasswordRequestDTO.builder().email("user@test.com").build());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        // Le message ne révèle pas si l'email existe ou non dans la base
        assertThat(body.get("message")).doesNotContain("token");
        assertThat(body.get("message")).doesNotContain("UUID");
    }

    @Test
    @DisplayName("forgotPassword : transmet exactement l'email au service")
    void forgotPassword_transmetEmailAuService() {
        when(passwordResetService.requestPasswordReset(any())).thenReturn("OK");

        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("client@test.com")
                .build();

        passwordResetController.forgotPassword(dto);

        ArgumentCaptor<ForgotPasswordRequestDTO> captor = ArgumentCaptor.forClass(ForgotPasswordRequestDTO.class);
        verify(passwordResetService).requestPasswordReset(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("client@test.com");
    }

    @Test
    @DisplayName("forgotPassword : le body de succes ne contient pas le token genere par le service")
    void forgotPassword_neRetournePasLeTokenDansLaReponse() {
        when(passwordResetService.requestPasswordReset(any()))
                .thenReturn("Email envoye avec token: token-secret-123");

        ResponseEntity<?> response = passwordResetController.forgotPassword(
                ForgotPasswordRequestDTO.builder().email("user@test.com").build());

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).doesNotContain("token-secret-123");
    }

    @Test
    @DisplayName("forgotPassword : email vide refuse par le service -> 400 BAD_REQUEST")
    void forgotPassword_emailVide_retourne400() {
        when(passwordResetService.requestPasswordReset(any()))
                .thenThrow(new RuntimeException("Utilisateur non trouvÃ©"));

        ResponseEntity<?> response = passwordResetController.forgotPassword(
                ForgotPasswordRequestDTO.builder().email("").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Utilisateur non trouvÃ©");
    }

    // =========================================================
    // POST /api/auth/reset-password
    // =========================================================

    @Test
    @DisplayName("resetPassword : token valide + nouveau mdp → 200 OK avec message succès")
    void resetPassword_tokenValide_retourne200AvecMessage() {
        when(passwordResetService.resetPassword(any(ResetPasswordRequestDTO.class)))
                .thenReturn("Mot de passe réinitialisé avec succès");

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("uuid-valide").nouveauMotDePasse("NouveauMdp123!").build();

        ResponseEntity<?> response = passwordResetController.resetPassword(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).contains("succès");
        verify(passwordResetService).resetPassword(dto);
    }

    @Test
    @DisplayName("resetPassword : service appelle resetPassword une seule fois")
    void resetPassword_appelleServiceUneSeuleFois() {
        when(passwordResetService.resetPassword(any()))
                .thenReturn("Mot de passe réinitialisé avec succès");

        passwordResetController.resetPassword(
                ResetPasswordRequestDTO.builder()
                        .token("t").nouveauMotDePasse("p").build());

        verify(passwordResetService, times(1)).resetPassword(any());
    }

    @Test
    @DisplayName("resetPassword : token invalide → 400 BAD_REQUEST")
    void resetPassword_tokenInvalide_retourne400() {
        when(passwordResetService.resetPassword(any()))
                .thenThrow(new RuntimeException("Token invalide"));

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("bad-token").nouveauMotDePasse("Mdp123!").build();

        ResponseEntity<?> response = passwordResetController.resetPassword(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Token invalide");
    }

    @Test
    @DisplayName("resetPassword : token expiré → 400 BAD_REQUEST")
    void resetPassword_tokenExpire_retourne400() {
        when(passwordResetService.resetPassword(any()))
                .thenThrow(new RuntimeException("Token expiré"));

        ResponseEntity<?> response = passwordResetController.resetPassword(
                ResetPasswordRequestDTO.builder()
                        .token("expire").nouveauMotDePasse("Mdp123!").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("Token expiré");
    }

    @Test
    @DisplayName("resetPassword : token déjà utilisé → 400 BAD_REQUEST")
    void resetPassword_tokenDejaUtilise_retourne400() {
        when(passwordResetService.resetPassword(any()))
                .thenThrow(new RuntimeException("Token déjà utilisé"));

        ResponseEntity<?> response = passwordResetController.resetPassword(
                ResetPasswordRequestDTO.builder()
                        .token("used").nouveauMotDePasse("Mdp123!").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("resetPassword : utilisateur introuvable → 400 BAD_REQUEST")
    void resetPassword_utilisateurInexistant_retourne400() {
        when(passwordResetService.resetPassword(any()))
                .thenThrow(new RuntimeException("Utilisateur non trouvé"));

        ResponseEntity<?> response = passwordResetController.resetPassword(
                ResetPasswordRequestDTO.builder()
                        .token("orphan").nouveauMotDePasse("Mdp123!").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
