package tn.comping.spring.backendcomping.TestController;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.comping.spring.backendcomping.controllers.ProfileController;
import tn.comping.spring.backendcomping.dto.UpdatePasswordDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.IProfileService;
import tn.comping.spring.backendcomping.services.serviceImpl.SignupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires (Mockito pur) - ProfileController (updatePassword)")
class ProfileControllerPasswordUnitTest {

    @Mock private IProfileService profileService;
    @Mock private SignupService signupService;

    @InjectMocks private ProfileController profileController;

    private UpdatePasswordDTO buildDto(String oldPassword, String newPassword, String confirmPassword) {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        dto.setConfirmPassword(confirmPassword);
        return dto;
    }

    // =========================================================
    // PUT /api/users/{userId}/password
    // =========================================================

    @Test
    @DisplayName("updatePassword : donnees valides -> 200 OK avec message du service")
    void updatePassword_donneesValides_retourne200AvecMessage() {
        UpdatePasswordDTO dto = buildDto("AncienMdp123!", "NouveauMdp456!", "NouveauMdp456!");
        when(profileService.updatePassword("user1", dto))
                .thenReturn("Mot de passe modifie avec succes");

        ResponseEntity<String> response = profileController.updatePassword("user1", dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Mot de passe modifie avec succes");
        verify(profileService).updatePassword("user1", dto);
    }

    @Test
    @DisplayName("updatePassword : delegue au service avec userId et DTO")
    void updatePassword_delegueAvecUserIdEtDto() {
        UpdatePasswordDTO dto = buildDto("old", "new", "new");
        when(profileService.updatePassword(eq("user1"), any(UpdatePasswordDTO.class)))
                .thenReturn("OK");

        profileController.updatePassword("user1", dto);

        verify(profileService).updatePassword("user1", dto);
    }

    @Test
    @DisplayName("updatePassword : utilisateur introuvable -> exception propagee")
    void updatePassword_utilisateurInexistant_exceptionPropagee() {
        UpdatePasswordDTO dto = buildDto("AncienMdp123!", "NouveauMdp456!", "NouveauMdp456!");
        when(profileService.updatePassword("inexistant", dto))
                .thenThrow(new RuntimeException("Utilisateur non trouve"));

        assertThatThrownBy(() -> profileController.updatePassword("inexistant", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Utilisateur non trouve");
    }

    @Test
    @DisplayName("updatePassword : ancien mot de passe incorrect -> exception propagee")
    void updatePassword_ancienMotDePasseIncorrect_exceptionPropagee() {
        UpdatePasswordDTO dto = buildDto("MauvaisMdp!", "NouveauMdp456!", "NouveauMdp456!");
        when(profileService.updatePassword("user1", dto))
                .thenThrow(new RuntimeException("Ancien mot de passe incorrect"));

        assertThatThrownBy(() -> profileController.updatePassword("user1", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ancien mot de passe incorrect");
    }

    @Test
    @DisplayName("updatePassword : confirmation differente -> exception propagee")
    void updatePassword_confirmationDifferente_exceptionPropagee() {
        UpdatePasswordDTO dto = buildDto("AncienMdp123!", "NouveauMdp456!", "AutreMdp789!");
        when(profileService.updatePassword("user1", dto))
                .thenThrow(new RuntimeException("Les mots de passe ne correspondent pas"));

        assertThatThrownBy(() -> profileController.updatePassword("user1", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Les mots de passe ne correspondent pas");
    }
}
