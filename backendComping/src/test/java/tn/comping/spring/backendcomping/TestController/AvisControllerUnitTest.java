package tn.comping.spring.backendcomping.TestController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.controllers.AvisController;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import tn.comping.spring.backendcomping.services.serviceImpl.AvisService;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires (Mockito pur) - AvisController")
class AvisControllerUnitTest {

    @Mock private AvisService avisService;

    @InjectMocks private AvisController avisController;

    private Authentication auth;
    private AvisResponseDTO avisResponse;
    private AvisRequestDTO avisRequest;

    @BeforeEach
    void setUp() {
        auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");

        avisResponse = AvisResponseDTO.builder()
                .id("avis1").note(4).commentaire("Excellent site")
                .utilisateurId("user1").utilisateurNom("Jean Dupont")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.EN_ATTENTE).valide(false)
                .datePublication(new Date()).build();

        avisRequest = AvisRequestDTO.builder()
                .note(4).commentaire("Excellent site")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();
    }

    // =========================================================
    // POST /api/avis  →  creerAvis
    // =========================================================

    @Test
    @DisplayName("creerAvis : délègue au service et retourne 201 CREATED")
    void creerAvis_delegueAuService_retourne201() {
        when(avisService.creerAvis(any(AvisRequestDTO.class), eq("user@test.com")))
                .thenReturn(avisResponse);

        ResponseEntity<AvisResponseDTO> response = avisController.creerAvis(avisRequest, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNote()).isEqualTo(4);
        verify(avisService).creerAvis(avisRequest, "user@test.com");
    }

    @Test
    @DisplayName("creerAvis : service lève exception → exception propagée")
    void creerAvis_serviceLanceException_propagee() {
        when(avisService.creerAvis(any(), eq("user@test.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Note invalide"));

        assertThatThrownBy(() -> avisController.creerAvis(avisRequest, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("creerAvis : email extrait de Authentication.getName()")
    void creerAvis_extrait_emailDepuisAuthentication() {
        when(avisService.creerAvis(any(), eq("user@test.com"))).thenReturn(avisResponse);

        avisController.creerAvis(avisRequest, auth);

        verify(auth).getName();
        verify(avisService).creerAvis(any(), eq("user@test.com"));
    }

    // =========================================================
    // GET /api/avis/{id}  →  getAvisById
    // =========================================================

    @Test
    @DisplayName("getAvisById : délègue au service et retourne 200 OK")
    void getAvisById_delegueAuService_retourne200() {
        when(avisService.getAvisById("avis1")).thenReturn(avisResponse);

        ResponseEntity<AvisResponseDTO> response = avisController.getAvisById("avis1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("avis1");
        verify(avisService).getAvisById("avis1");
    }

    @Test
    @DisplayName("getAvisById : ID inexistant → exception propagée du service")
    void getAvisById_idInexistant_exceptionPropagee() {
        when(avisService.getAvisById("inexistant"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        assertThatThrownBy(() -> avisController.getAvisById("inexistant"))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // GET /api/avis/cible/{cibleId}  →  getAvisByCible
    // =========================================================

    @Test
    @DisplayName("getAvisByCible : retourne liste avec 200 OK")
    void getAvisByCible_retourneListe200() {
        when(avisService.getAvisByCible("site1", "SITE_CAMPING"))
                .thenReturn(List.of(avisResponse));

        ResponseEntity<List<AvisResponseDTO>> response =
                avisController.getAvisByCible("site1", "SITE_CAMPING");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(avisService).getAvisByCible("site1", "SITE_CAMPING");
    }

    @Test
    @DisplayName("getAvisByCible : liste vide → 200 OK avec liste vide")
    void getAvisByCible_listeVide_retourne200AvecListeVide() {
        when(avisService.getAvisByCible("site2", "PRODUIT")).thenReturn(List.of());

        ResponseEntity<List<AvisResponseDTO>> response =
                avisController.getAvisByCible("site2", "PRODUIT");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    // GET /api/avis/mes-avis  →  getMesAvis
    // =========================================================

    @Test
    @DisplayName("getMesAvis : retourne les avis de l'utilisateur connecté")
    void getMesAvis_retourneAvisUtilisateurConnecte() {
        when(avisService.getMesAvis("user@test.com")).thenReturn(List.of(avisResponse));

        ResponseEntity<List<AvisResponseDTO>> response = avisController.getMesAvis(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(avisService).getMesAvis("user@test.com");
    }

    // =========================================================
    // GET /api/avis/statut/{statut}  →  getAvisByStatut
    // =========================================================

    @Test
    @DisplayName("getAvisByStatut : EN_ATTENTE → retourne avis en attente")
    void getAvisByStatut_enAttente_retourneAvis() {
        when(avisService.getAvisByStatut(StatutAvis.EN_ATTENTE))
                .thenReturn(List.of(avisResponse));

        ResponseEntity<List<AvisResponseDTO>> response =
                avisController.getAvisByStatut(StatutAvis.EN_ATTENTE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(avisService).getAvisByStatut(StatutAvis.EN_ATTENTE);
    }

    // =========================================================
    // PUT /api/avis/{id}  →  updateAvis
    // =========================================================

    @Test
    @DisplayName("updateAvis : délègue au service avec id, dto et email")
    void updateAvis_delegueAvecParametresCorrects() {
        AvisResponseDTO avisMaj = AvisResponseDTO.builder()
                .id("avis1").note(5).commentaire("Parfait!").build();
        when(avisService.updateAvis(eq("avis1"), any(AvisRequestDTO.class), eq("user@test.com")))
                .thenReturn(avisMaj);

        ResponseEntity<AvisResponseDTO> response =
                avisController.updateAvis("avis1", avisRequest, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getNote()).isEqualTo(5);
        verify(avisService).updateAvis("avis1", avisRequest, "user@test.com");
    }

    @Test
    @DisplayName("updateAvis : service lève 403 → exception propagée")
    void updateAvis_serviceLance403_propagee() {
        when(avisService.updateAvis(eq("avis1"), any(), eq("user@test.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        assertThatThrownBy(() -> avisController.updateAvis("avis1", avisRequest, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // DELETE /api/avis/{id}  →  deleteAvis
    // =========================================================

    @Test
    @DisplayName("deleteAvis : délègue au service et retourne 204 No Content")
    void deleteAvis_delegueAuService_retourne204() {
        doNothing().when(avisService).deleteAvis("avis1", "user@test.com");

        ResponseEntity<Void> response = avisController.deleteAvis("avis1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(avisService).deleteAvis("avis1", "user@test.com");
    }

    @Test
    @DisplayName("deleteAvis : service lève 403 → exception propagée")
    void deleteAvis_serviceLance403_propagee() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"))
                .when(avisService).deleteAvis("avis1", "user@test.com");

        assertThatThrownBy(() -> avisController.deleteAvis("avis1", auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // POST /api/avis/{id}/valider  →  validerAvis
    // =========================================================

    @Test
    @DisplayName("validerAvis : délègue au service et retourne avis validé")
    void validerAvis_delegueAuService_retourneAvisValide() {
        AvisResponseDTO avisValide = AvisResponseDTO.builder()
                .id("avis1").statut(StatutAvis.VALIDE).valide(true).build();
        Authentication adminAuth = mock(Authentication.class);
        when(adminAuth.getName()).thenReturn("admin@test.com");
        when(avisService.validerAvis("avis1", "admin@test.com")).thenReturn(avisValide);

        ResponseEntity<AvisResponseDTO> response = avisController.validerAvis("avis1", adminAuth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValide()).isTrue();
        verify(avisService).validerAvis("avis1", "admin@test.com");
    }

    // =========================================================
    // POST /api/avis/{id}/rejeter  →  rejeterAvis
    // =========================================================

    @Test
    @DisplayName("rejeterAvis : délègue au service avec motif et retourne avis rejeté")
    void rejeterAvis_delegueAvecMotif_retourneAvisRejete() {
        AvisResponseDTO avisRejete = AvisResponseDTO.builder()
                .id("avis1").statut(StatutAvis.REJETE).valide(false).build();
        Authentication adminAuth = mock(Authentication.class);
        when(adminAuth.getName()).thenReturn("admin@test.com");
        when(avisService.rejeterAvis("avis1", "Contenu inapproprié", "admin@test.com"))
                .thenReturn(avisRejete);

        ResponseEntity<AvisResponseDTO> response =
                avisController.rejeterAvis("avis1", "Contenu inapproprié", adminAuth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isValide()).isFalse();
        verify(avisService).rejeterAvis("avis1", "Contenu inapproprié", "admin@test.com");
    }

    // =========================================================
    // GET /api/avis/valides  →  getAvisValides
    // =========================================================

    @Test
    @DisplayName("getAvisValides : retourne uniquement les avis validés")
    void getAvisValides_retourneAvisValides() {
        AvisResponseDTO avisValide = AvisResponseDTO.builder()
                .id("av1").statut(StatutAvis.VALIDE).valide(true).build();
        when(avisService.getAvisValides()).thenReturn(List.of(avisValide));

        ResponseEntity<List<AvisResponseDTO>> response = avisController.getAvisValides();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).isValide()).isTrue();
        verify(avisService).getAvisValides();
    }

    // =========================================================
    // GET /api/avis/amis  →  getAvisAmis
    // =========================================================

    @Test
    @DisplayName("getAvisAmis : retourne les avis des amis")
    void getAvisAmis_retourneAvisAmis() {
        when(avisService.getAvisAmis("user@test.com")).thenReturn(List.of(avisResponse));

        ResponseEntity<List<AvisResponseDTO>> response = avisController.getAvisAmis(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(avisService).getAvisAmis("user@test.com");
    }

    // =========================================================
    // GET /api/avis/statistiques/{cibleId}  →  getStatistiquesAvis
    // =========================================================

    @Test
    @DisplayName("getStatistiquesAvis : retourne les statistiques calculées")
    void getStatistiquesAvis_retourneStatistiques() {
        StatistiquesAvisDTO stats = StatistiquesAvisDTO.builder()
                .nombreTotal(10).noteMoyenne(4.2)
                .nombre5Etoiles(5).nombre4Etoiles(3)
                .nombre3Etoiles(2).build();
        when(avisService.getStatistiquesAvis("site1", "SITE_CAMPING")).thenReturn(stats);

        ResponseEntity<StatistiquesAvisDTO> response =
                avisController.getStatistiquesAvis("site1", "SITE_CAMPING");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getNombreTotal()).isEqualTo(10);
        assertThat(response.getBody().getNoteMoyenne()).isEqualTo(4.2);
        verify(avisService).getStatistiquesAvis("site1", "SITE_CAMPING");
    }
}
