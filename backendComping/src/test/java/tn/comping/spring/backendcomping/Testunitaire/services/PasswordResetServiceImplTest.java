package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.PasswordResetTokenRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailService;
import tn.comping.spring.backendcomping.services.serviceImpl.PasswordResetServiceImpl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires - PasswordResetServiceImpl")
class PasswordResetServiceImplTest {

    @Mock private SignupRepository signupRepository;
    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks private PasswordResetServiceImpl passwordResetService;

    private SignupEntity utilisateur;
    private PasswordResetToken tokenValide;
    private ForgotPasswordRequestDTO forgotDto;
    private ResetPasswordRequestDTO resetDto;

    @BeforeEach
    void setUp() {
        utilisateur = SignupEntity.builder()
                .id("user1")
                .email("user@test.com")
                .firstName("Jean")
                .lastName("Dupont")
                .password("$2a$10$encodedPassword")
                .build();

        tokenValide = PasswordResetToken.builder()
                .id("token1")
                .userId("user1")
                .token("uuid-token-valide")
                .expiryDate(Date.from(Instant.now().plusSeconds(3600)))
                .used(false)
                .build();

        forgotDto = ForgotPasswordRequestDTO.builder()
                .email("user@test.com")
                .build();

        resetDto = ResetPasswordRequestDTO.builder()
                .token("uuid-token-valide")
                .nouveauMotDePasse("NouveauMdp123!")
                .build();
    }

    // =========================================================
    // requestPasswordReset
    // =========================================================

    @Test
    @DisplayName("requestPasswordReset : email existant → token créé et email envoyé")
    void requestPasswordReset_emailExistant_retourneSucces() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        String result = passwordResetService.requestPasswordReset(forgotDto);

        assertThat(result).contains("token");
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(
                eq("user@test.com"), anyString(), eq("Jean"));
    }

    @Test
    @DisplayName("requestPasswordReset : email inexistant → RuntimeException")
    void requestPasswordReset_emailInexistant_lanceException() {
        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("inconnu@test.com").build();

        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("requestPasswordReset : supprime les anciens tokens avant d'en créer un nouveau")
    void requestPasswordReset_supprimeAnciensTokens() {
        PasswordResetToken ancienToken = PasswordResetToken.builder()
                .id("old1").userId("user1").token("old-token")
                .expiryDate(Date.from(Instant.now().minusSeconds(7200)))
                .used(false).build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of(ancienToken));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        passwordResetService.requestPasswordReset(forgotDto);

        verify(tokenRepository).deleteAll(List.of(ancienToken));
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("requestPasswordReset : le token généré a une expiration à 1 heure")
    void requestPasswordReset_tokenExpirationDansUneHeure() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        passwordResetService.requestPasswordReset(forgotDto);

        verify(tokenRepository).save(argThat(token ->
                token.getExpiryDate().after(new Date()) &&
                token.getExpiryDate().before(Date.from(Instant.now().plusSeconds(3700)))));
    }

    @Test
    @DisplayName("requestPasswordReset : plusieurs anciens tokens → tous supprimés")
    void requestPasswordReset_plusieursAnciensTokens_tousSupprimés() {
        PasswordResetToken t1 = PasswordResetToken.builder().id("t1").userId("user1")
                .token("tok1").expiryDate(new Date()).used(true).build();
        PasswordResetToken t2 = PasswordResetToken.builder().id("t2").userId("user1")
                .token("tok2").expiryDate(new Date()).used(false).build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of(t1, t2));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        passwordResetService.requestPasswordReset(forgotDto);

        verify(tokenRepository).deleteAll(argThat(list ->
                ((List<?>) list).size() == 2));
    }

    @Test
    @DisplayName("requestPasswordReset : email vide -> RuntimeException sans token ni email")
    void requestPasswordReset_emailVide_lanceExceptionSansEffet() {
        ForgotPasswordRequestDTO dto = ForgotPasswordRequestDTO.builder()
                .email("")
                .build();
        when(signupRepository.findByEmail("")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouv");

        verify(tokenRepository, never()).findByUserId(anyString());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("requestPasswordReset : nouveau token lie au bon utilisateur et non utilise")
    void requestPasswordReset_nouveauTokenContientBonUserEtUsedFalse() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        passwordResetService.requestPasswordReset(forgotDto);

        verify(tokenRepository).save(argThat(token ->
                "user1".equals(token.getUserId()) &&
                token.getToken() != null &&
                !token.getToken().isBlank() &&
                !token.isUsed()));
    }

    @Test
    @DisplayName("requestPasswordReset : envoie l'email avec email, token sauvegarde et prenom")
    void requestPasswordReset_emailContientTokenSauvegardeEtPrenom() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        passwordResetService.requestPasswordReset(forgotDto);

        verify(emailService).sendPasswordResetEmail(
                eq("user@test.com"),
                argThat(token -> token != null && !token.isBlank()),
                eq("Jean"));
    }

    @Test
    @DisplayName("requestPasswordReset : erreur email propagee apres creation du token")
    void requestPasswordReset_erreurEmail_propageException() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUserId("user1")).thenReturn(List.of());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP indisponible"))
                .when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> passwordResetService.requestPasswordReset(forgotDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP indisponible");

        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    // =========================================================
    // resetPassword
    // =========================================================

    @Test
    @DisplayName("resetPassword : token valide → mot de passe réinitialisé")
    void resetPassword_tokenValide_retourneSucces() {
        when(tokenRepository.findByToken("uuid-token-valide")).thenReturn(Optional.of(tokenValide));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode("NouveauMdp123!")).thenReturn("$2a$10$nouveauEncoded");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);

        String result = passwordResetService.resetPassword(resetDto);

        assertThat(result).contains("succès");
        verify(signupRepository).save(any(SignupEntity.class));
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("resetPassword : token inexistant → RuntimeException")
    void resetPassword_tokenInexistant_lanceException() {
        when(tokenRepository.findByToken("token-invalide")).thenReturn(Optional.empty());

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("token-invalide").nouveauMotDePasse("Mdp123!").build();

        assertThatThrownBy(() -> passwordResetService.resetPassword(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token invalide");

        verify(signupRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword : token déjà utilisé → RuntimeException")
    void resetPassword_tokenDejaUtilise_lanceException() {
        PasswordResetToken tokenUtilise = PasswordResetToken.builder()
                .id("t1").userId("user1").token("token-utilise")
                .expiryDate(Date.from(Instant.now().plusSeconds(3600)))
                .used(true).build();

        when(tokenRepository.findByToken("token-utilise")).thenReturn(Optional.of(tokenUtilise));

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("token-utilise").nouveauMotDePasse("Mdp123!").build();

        assertThatThrownBy(() -> passwordResetService.resetPassword(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("déjà utilisé");

        verify(signupRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword : token expiré → RuntimeException")
    void resetPassword_tokenExpire_lanceException() {
        PasswordResetToken tokenExpire = PasswordResetToken.builder()
                .id("t1").userId("user1").token("token-expire")
                .expiryDate(Date.from(Instant.now().minusSeconds(3600))) // 1h dans le passé
                .used(false).build();

        when(tokenRepository.findByToken("token-expire")).thenReturn(Optional.of(tokenExpire));

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("token-expire").nouveauMotDePasse("Mdp123!").build();

        assertThatThrownBy(() -> passwordResetService.resetPassword(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("expiré");

        verify(signupRepository, never()).save(any());
    }

    @Test
    @DisplayName("resetPassword : utilisateur introuvable avec token valide → RuntimeException")
    void resetPassword_utilisateurInexistant_lanceException() {
        when(tokenRepository.findByToken("uuid-token-valide")).thenReturn(Optional.of(tokenValide));
        when(signupRepository.findById("user1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.resetPassword(resetDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(signupRepository, never()).save(any(SignupEntity.class));
    }

    @Test
    @DisplayName("resetPassword : nouveau mot de passe est encodé avant sauvegarde")
    void resetPassword_encodeNouveauMotDePasse() {
        when(tokenRepository.findByToken("uuid-token-valide")).thenReturn(Optional.of(tokenValide));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode("NouveauMdp123!")).thenReturn("$2a$10$nouveauEncoded");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);

        passwordResetService.resetPassword(resetDto);

        verify(passwordEncoder).encode("NouveauMdp123!");
        verify(signupRepository).save(argThat(u ->
                "$2a$10$nouveauEncoded".equals(u.getPassword())));
    }

    @Test
    @DisplayName("resetPassword : token marqué comme utilisé après réinitialisation réussie")
    void resetPassword_marqueTokenUtilise() {
        when(tokenRepository.findByToken("uuid-token-valide")).thenReturn(Optional.of(tokenValide));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenValide);

        passwordResetService.resetPassword(resetDto);

        verify(tokenRepository).save(argThat(PasswordResetToken::isUsed));
    }

    @Test
    @DisplayName("resetPassword : token juste avant expiration → succès")
    void resetPassword_tokenJustAvantExpiration_retourneSucces() {
        PasswordResetToken tokenPresqueExpire = PasswordResetToken.builder()
                .id("t1").userId("user1").token("token-presque-expire")
                .expiryDate(Date.from(Instant.now().plusSeconds(5))) // expire dans 5 secondes
                .used(false).build();

        when(tokenRepository.findByToken("token-presque-expire")).thenReturn(Optional.of(tokenPresqueExpire));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(tokenPresqueExpire);

        ResetPasswordRequestDTO dto = ResetPasswordRequestDTO.builder()
                .token("token-presque-expire").nouveauMotDePasse("Mdp123!").build();

        String result = passwordResetService.resetPassword(dto);

        assertThat(result).contains("succès");
    }
}
