package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.ProfileServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ProfileServiceImpl (updatePassword)")
class ProfileServiceImplPasswordTest {

    @Mock private SignupRepository signupRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ProfileServiceImpl profileService;

    private SignupEntity utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = SignupEntity.builder()
                .id("user1")
                .email("user@test.com")
                .firstName("Jean")
                .lastName("Dupont")
                .password("$2a$10$ancienMotDePasseEncode")
                .build();
    }

    // =========================================================
    // updatePassword
    // =========================================================

    @Test
    @DisplayName("updatePassword : données valides → mot de passe mis à jour")
    void updatePassword_donneesValides_retourneSucces() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("AncienMdp123!");
        dto.setNewPassword("NouveauMdp456!");
        dto.setConfirmPassword("NouveauMdp456!");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("AncienMdp123!", "$2a$10$ancienMotDePasseEncode")).thenReturn(true);
        when(passwordEncoder.encode("NouveauMdp456!")).thenReturn("$2a$10$nouveauEncode");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);

        String result = profileService.updatePassword("user1", dto);

        assertThat(result).contains("succès");
        verify(signupRepository).save(any(SignupEntity.class));
    }

    @Test
    @DisplayName("updatePassword : utilisateur introuvable → RuntimeException")
    void updatePassword_utilisateurInexistant_lanceException() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("Mdp123!");
        dto.setNewPassword("Mdp456!");
        dto.setConfirmPassword("Mdp456!");

        when(signupRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updatePassword("inexistant", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(signupRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePassword : ancien mot de passe incorrect → RuntimeException")
    void updatePassword_ancienMotDePasseIncorrect_lanceException() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("MauvaisMdp!");
        dto.setNewPassword("NouveauMdp456!");
        dto.setConfirmPassword("NouveauMdp456!");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("MauvaisMdp!", "$2a$10$ancienMotDePasseEncode")).thenReturn(false);

        assertThatThrownBy(() -> profileService.updatePassword("user1", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ancien mot de passe incorrect");

        verify(signupRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("updatePassword : confirmation différente du nouveau mot de passe → RuntimeException")
    void updatePassword_confirmationDifferente_lanceException() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("AncienMdp123!");
        dto.setNewPassword("NouveauMdp456!");
        dto.setConfirmPassword("AutreMdp789!");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("AncienMdp123!", "$2a$10$ancienMotDePasseEncode")).thenReturn(true);

        assertThatThrownBy(() -> profileService.updatePassword("user1", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ne correspondent pas");

        verify(signupRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("updatePassword : le nouveau mot de passe est encodé avant sauvegarde")
    void updatePassword_encodeNouveauMotDePasse() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("AncienMdp123!");
        dto.setNewPassword("NouveauMdp456!");
        dto.setConfirmPassword("NouveauMdp456!");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("AncienMdp123!", "$2a$10$ancienMotDePasseEncode")).thenReturn(true);
        when(passwordEncoder.encode("NouveauMdp456!")).thenReturn("$2a$10$nouveauEncode");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);

        profileService.updatePassword("user1", dto);

        verify(passwordEncoder).encode("NouveauMdp456!");
        verify(signupRepository).save(argThat(u ->
                "$2a$10$nouveauEncode".equals(u.getPassword())));
    }

    @Test
    @DisplayName("updatePassword : confirmation vide → RuntimeException")
    void updatePassword_confirmationVide_lanceException() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("AncienMdp123!");
        dto.setNewPassword("NouveauMdp456!");
        dto.setConfirmPassword("");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("AncienMdp123!", "$2a$10$ancienMotDePasseEncode")).thenReturn(true);

        assertThatThrownBy(() -> profileService.updatePassword("user1", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ne correspondent pas");
    }

    @Test
    @DisplayName("updatePassword : même mot de passe ancien et nouveau → accepté si confirmation correspond")
    void updatePassword_ancienEgalNouveau_accepteSiConfirmationCorrecte() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword("MemeMdp123!");
        dto.setNewPassword("MemeMdp123!");
        dto.setConfirmPassword("MemeMdp123!");

        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("MemeMdp123!", "$2a$10$ancienMotDePasseEncode")).thenReturn(true);
        when(passwordEncoder.encode("MemeMdp123!")).thenReturn("$2a$10$encoded");
        when(signupRepository.save(any(SignupEntity.class))).thenReturn(utilisateur);

        String result = profileService.updatePassword("user1", dto);

        assertThat(result).contains("succès");
    }
}
