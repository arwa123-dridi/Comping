package tn.comping.spring.backendcomping.TestIntegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tn.comping.spring.backendcomping.dto.ForgotPasswordRequestDTO;
import tn.comping.spring.backendcomping.dto.ResetPasswordRequestDTO;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;
import tn.comping.spring.backendcomping.entities.Role;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.PasswordResetTokenRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.EmailService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "youtube.api.key=test-youtube-key",
        "groq.api.key=test-groq-key",
        "groq.api.url=https://example.test/groq",
        "groq.model=test-model",
        "ai.rss.feeds=https://example.test/feed.xml",
        "ia.api.url=http://localhost:5000/recommend",
        "stripe.secret.key=sk_test_dummy",
        "stripe.webhook.secret=whsec_test_dummy",
        "cloudinary.cloud.name=test-cloud",
        "cloudinary.api.key=123456789",
        "cloudinary.api.secret=test-secret"
})
@AutoConfigureMockMvc
@DisplayName("Tests integration complete API -> Service -> Repository -> Mongo - PasswordReset")
class PasswordResetApiFullIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SignupRepository signupRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean private EmailService emailService;
    @MockBean private JavaMailSender javaMailSender;

    private SignupEntity user;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        signupRepository.deleteAll();

        user = signupRepository.save(SignupEntity.builder()
                .firstName("Test")
                .lastName("User")
                .email("integration.user@test.com")
                .password(passwordEncoder.encode("AncienMdp123!"))
                .role(Role.USER)
                .statut(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        signupRepository.deleteAll();
    }

    @Test
    @DisplayName("forgot-password : appel API cree un token en base via service et repository")
    void forgotPassword_appelApiCreeTokenEnBase() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
                .email("integration.user@test.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        List<PasswordResetToken> tokens = tokenRepository.findByUserId(user.getId());
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getToken()).isNotBlank();
        assertThat(tokens.get(0).isUsed()).isFalse();
        assertThat(tokens.get(0).getExpiryDate()).isAfter(new java.util.Date());
        verify(emailService).sendPasswordResetEmail(
                anyString(),
                anyString(),
                anyString());
    }

    @Test
    @DisplayName("forgot-password : email inconnu retourne 400 sans creer de token")
    void forgotPassword_emailInconnu_retourne400SansToken() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
                .email("inconnu@test.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Utilisateur non trouv")));

        assertThat(tokenRepository.findAll()).isEmpty();
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("forgot-password : deuxieme demande remplace les anciens tokens du user")
    void forgotPassword_deuxiemeDemandeRemplaceAnciensTokens() throws Exception {
        PasswordResetToken ancienToken = tokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .token("ancien-token")
                .expiryDate(java.util.Date.from(java.time.Instant.now().plusSeconds(3600)))
                .used(false)
                .build());

        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
                .email("integration.user@test.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        List<PasswordResetToken> tokens = tokenRepository.findByUserId(user.getId());
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getId()).isNotEqualTo(ancienToken.getId());
        assertThat(tokens.get(0).getToken()).isNotEqualTo("ancien-token");
        assertThat(tokens.get(0).isUsed()).isFalse();
    }

    @Test
    @DisplayName("forgot-password : reponse API ne fuit pas le token genere")
    void forgotPassword_reponseApiNeRetournePasLeToken() throws Exception {
        ForgotPasswordRequestDTO request = ForgotPasswordRequestDTO.builder()
                .email("integration.user@test.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("token"))));

        List<PasswordResetToken> tokens = tokenRepository.findByUserId(user.getId());
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("reset-password : appel API met a jour le mot de passe et marque le token utilise")
    void resetPassword_appelApiMetAJourMotDePasseEtToken() throws Exception {
        PasswordResetToken token = tokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .token("token-integration-valide")
                .expiryDate(java.util.Date.from(java.time.Instant.now().plusSeconds(3600)))
                .used(false)
                .build());

        ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
                .token(token.getToken())
                .nouveauMotDePasse("NouveauMdp456!")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        SignupEntity updatedUser = signupRepository.findById(user.getId()).orElseThrow();
        PasswordResetToken updatedToken = tokenRepository.findByToken(token.getToken()).orElseThrow();

        assertThat(passwordEncoder.matches("NouveauMdp456!", updatedUser.getPassword())).isTrue();
        assertThat(updatedToken.isUsed()).isTrue();
    }

    @Test
    @DisplayName("reset-password : token invalide retourne 400 sans modifier le user")
    void resetPassword_tokenInvalide_retourne400SansModifierUser() throws Exception {
        ResetPasswordRequestDTO request = ResetPasswordRequestDTO.builder()
                .token("token-inexistant")
                .nouveauMotDePasse("NouveauMdp456!")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token invalide"));

        SignupEntity unchangedUser = signupRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("AncienMdp123!", unchangedUser.getPassword())).isTrue();
    }
}
