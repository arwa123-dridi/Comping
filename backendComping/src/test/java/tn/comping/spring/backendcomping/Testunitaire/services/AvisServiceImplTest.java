




package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.AvisServiceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AvisServiceImpl")
class AvisServiceImplTest {

    @Mock private AvisRepository avisRepository;
    @Mock private SignupRepository signupRepository;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private AvisServiceImpl avisService;

    private SignupEntity utilisateur;
    private SignupEntity admin;
    private Avis avis;
    private AvisRequestDTO validDto;

    @BeforeEach
    void setUp() {
        utilisateur = SignupEntity.builder()
                .id("user1")
                .email("user@test.com")
                .firstName("Jean")
                .lastName("Dupont")
                .role(Role.USER)
                .build();

        admin = SignupEntity.builder()
                .id("admin1")
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("System")
                .role(Role.ADMIN)
                .build();

        avis = Avis.builder()
                .id("avis1")
                .note(4)
                .commentaire("Excellent site")
                .utilisateurId("user1")
                .cibleId("site1")
                .typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.EN_ATTENTE)
                .valide(false)
                .datePublication(new Date())
                .build();

        validDto = AvisRequestDTO.builder()
                .note(4)
                .commentaire("Excellent site")
                .cibleId("site1")
                .typeCible(TypeCible.SITE_CAMPING)
                .build();
    }

    // =========================================================
    // creerAvis - validation
    // =========================================================

    @Test
    @DisplayName("creerAvis : données valides → avis créé et sauvegardé")
    void creerAvis_donneesValides_retourneAvisCree() {
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.save(any(Avis.class))).thenReturn(avis);
        when(signupRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        AvisResponseDTO result = avisService.creerAvis(validDto, "user@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getNote()).isEqualTo(4);
        assertThat(result.getCommentaire()).isEqualTo("Excellent site");
        verify(avisRepository).save(any(Avis.class));
    }

    @Test
    @DisplayName("creerAvis : note < 1 → exception 400")
    void creerAvis_noteInferieure1_lanceException400() {
        AvisRequestDTO dtoInvalide = AvisRequestDTO.builder()
                .note(0).commentaire("Commentaire").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        assertThatThrownBy(() -> avisService.creerAvis(dtoInvalide, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : note > 5 → exception 400")
    void creerAvis_noteSuperieure5_lanceException400() {
        AvisRequestDTO dtoInvalide = AvisRequestDTO.builder()
                .note(6).commentaire("Commentaire").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        assertThatThrownBy(() -> avisService.creerAvis(dtoInvalide, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : commentaire vide → exception 400")
    void creerAvis_commentaireVide_lanceException400() {
        AvisRequestDTO dtoInvalide = AvisRequestDTO.builder()
                .note(3).commentaire("   ").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        assertThatThrownBy(() -> avisService.creerAvis(dtoInvalide, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : cibleId vide → exception 400")
    void creerAvis_cibleIdVide_lanceException400() {
        AvisRequestDTO dtoInvalide = AvisRequestDTO.builder()
                .note(3).commentaire("Bon").cibleId("").typeCible(TypeCible.SITE_CAMPING).build();

        assertThatThrownBy(() -> avisService.creerAvis(dtoInvalide, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : typeCible null → exception 400")
    void creerAvis_typeCibleNull_lanceException400() {
        AvisRequestDTO dtoInvalide = AvisRequestDTO.builder()
                .note(3).commentaire("Bon").cibleId("site1").typeCible(null).build();

        assertThatThrownBy(() -> avisService.creerAvis(dtoInvalide, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : utilisateur introuvable → exception 404")
    void creerAvis_utilisateurInexistant_lanceException404() {
        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.creerAvis(validDto, "inconnu@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("creerAvis : parentAvisId inexistant → exception 400")
    void creerAvis_parentAvisIdInexistant_lanceException400() {
        AvisRequestDTO dtoAvecParent = AvisRequestDTO.builder()
                .note(3).commentaire("Réponse").cibleId("site1")
                .typeCible(TypeCible.SITE_CAMPING).parentAvisId("parentInexistant").build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findById("parentInexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.creerAvis(dtoAvecParent, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("creerAvis : réponse à un avis valide → réponse créée avec parentAvisId")
    void creerAvis_reponseAvecParentValide_retourneReponseAvecParent() {
        Avis parent = Avis.builder()
                .id("parentId").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        AvisRequestDTO dtoReponse = AvisRequestDTO.builder()
                .note(3).commentaire("Réponse").cibleId("site1")
                .typeCible(TypeCible.SITE_CAMPING).parentAvisId("parentId").build();

        Avis avisReponse = Avis.builder()
                .id("avis2").note(3).commentaire("Réponse")
                .utilisateurId("user1").cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .parentAvisId("parentId").statut(StatutAvis.EN_ATTENTE).datePublication(new Date()).build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findById("parentId")).thenReturn(Optional.of(parent));
        when(avisRepository.save(any(Avis.class))).thenReturn(avisReponse);
        when(signupRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        AvisResponseDTO result = avisService.creerAvis(dtoReponse, "user@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getParentAvisId()).isEqualTo("parentId");
    }

    @Test
    @DisplayName("creerAvis : réponse dont la cible diffère du parent → exception 400")
    void creerAvis_reponseAvecCibleDifferente_lanceException400() {
        Avis parent = Avis.builder()
                .id("parentId").cibleId("site_autre").typeCible(TypeCible.SITE_CAMPING).build();

        AvisRequestDTO dtoMauvaiseCible = AvisRequestDTO.builder()
                .note(3).commentaire("Réponse").cibleId("site1")
                .typeCible(TypeCible.SITE_CAMPING).parentAvisId("parentId").build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findById("parentId")).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> avisService.creerAvis(dtoMauvaiseCible, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // =========================================================
    // getAvisById
    // =========================================================

    @Test
    @DisplayName("getAvisById : ID existant → avis retourné avec enfants")
    void getAvisById_idExistant_retourneAvisAvecEnfants() {
        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByParentAvisIdOrderByDatePublicationDesc("avis1")).thenReturn(List.of());

        AvisResponseDTO result = avisService.getAvisById("avis1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("avis1");
        assertThat(result.getNote()).isEqualTo(4);
        assertThat(result.getEnfants()).isEmpty();
    }

    @Test
    @DisplayName("getAvisById : ID inexistant → exception 404")
    void getAvisById_idInexistant_lanceException404() {
        when(avisRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.getAvisById("inexistant"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // getAvisByCible
    // =========================================================

    @Test
    @DisplayName("getAvisByCible : typeCible valide → liste d'avis retournée")
    void getAvisByCible_typeCibleValide_retourneListeAvis() {
        when(avisRepository.findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                "site1", TypeCible.SITE_CAMPING, true))
                .thenReturn(List.of(avis));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByParentAvisIdOrderByDatePublicationDesc("avis1")).thenReturn(List.of());

        List<AvisResponseDTO> result = avisService.getAvisByCible("site1", "SITE_CAMPING");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCibleId()).isEqualTo("site1");
    }

    @Test
    @DisplayName("getAvisByCible : typeCible invalide → exception 400")
    void getAvisByCible_typeCibleInvalide_lanceException400() {
        assertThatThrownBy(() -> avisService.getAvisByCible("site1", "TYPE_INVALIDE"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("getAvisByCible : typeCible null → exception 400")
    void getAvisByCible_typeCibleNull_lanceException400() {
        assertThatThrownBy(() -> avisService.getAvisByCible("site1", null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // =========================================================
    // getMesAvis
    // =========================================================

    @Test
    @DisplayName("getMesAvis : utilisateur existant → liste de ses avis")
    void getMesAvis_utilisateurExistant_retourneListeAvis() {
        Avis avis2 = Avis.builder().id("avis2").note(3).commentaire("Bien")
                .utilisateurId("user1").cibleId("site2").typeCible(TypeCible.PRODUIT)
                .statut(StatutAvis.EN_ATTENTE).datePublication(new Date()).build();

        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByUtilisateurIdOrderByDatePublicationDesc("user1"))
                .thenReturn(List.of(avis, avis2));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        List<AvisResponseDTO> result = avisService.getMesAvis("user@test.com");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getMesAvis : utilisateur inexistant → exception 404")
    void getMesAvis_utilisateurInexistant_lanceException404() {
        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.getMesAvis("inconnu@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // updateAvis
    // =========================================================

    @Test
    @DisplayName("updateAvis : auteur modifie son avis → avis mis à jour")
    void updateAvis_parAuteur_retourneAvisMisAJour() {
        AvisRequestDTO updateDto = AvisRequestDTO.builder()
                .note(5).commentaire("Parfait!").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        Avis avisMaj = Avis.builder().id("avis1").note(5).commentaire("Parfait!")
                .utilisateurId("user1").cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.EN_ATTENTE).datePublication(new Date()).build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.save(any(Avis.class))).thenReturn(avisMaj);
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        AvisResponseDTO result = avisService.updateAvis("avis1", updateDto, "user@test.com");

        assertThat(result).isNotNull();
        verify(avisRepository).save(any(Avis.class));
    }

    @Test
    @DisplayName("updateAvis : autre utilisateur modifie → exception 403")
    void updateAvis_parAutreUtilisateur_lanceException403() {
        SignupEntity autreUser = SignupEntity.builder().id("user2").email("autre@test.com").build();
        AvisRequestDTO updateDto = AvisRequestDTO.builder()
                .note(5).commentaire("Parfait!").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("autre@test.com")).thenReturn(Optional.of(autreUser));

        assertThatThrownBy(() -> avisService.updateAvis("avis1", updateDto, "autre@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(avisRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateAvis : avis inexistant → exception 404")
    void updateAvis_avisInexistant_lanceException404() {
        AvisRequestDTO updateDto = AvisRequestDTO.builder()
                .note(5).commentaire("Ok").cibleId("site1").typeCible(TypeCible.SITE_CAMPING).build();
        when(avisRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.updateAvis("inexistant", updateDto, "user@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // deleteAvis
    // =========================================================

    @Test
    @DisplayName("deleteAvis : auteur supprime son avis → suppression effectuée")
    void deleteAvis_parAuteur_suppressionEffectuee() {
        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByParentAvisIdOrderByDatePublicationDesc("avis1")).thenReturn(List.of());

        avisService.deleteAvis("avis1", "user@test.com");

        verify(avisRepository).deleteById("avis1");
    }

    @Test
    @DisplayName("deleteAvis : suppression en cascade des enfants")
    void deleteAvis_avecEnfants_supprimeEnfantsEtParent() {
        Avis enfant = Avis.builder().id("enfant1").utilisateurId("user1")
                .parentAvisId("avis1").datePublication(new Date()).build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("user@test.com")).thenReturn(Optional.of(utilisateur));
        when(avisRepository.findByParentAvisIdOrderByDatePublicationDesc("avis1")).thenReturn(List.of(enfant));
        when(avisRepository.findByParentAvisIdOrderByDatePublicationDesc("enfant1")).thenReturn(List.of());

        avisService.deleteAvis("avis1", "user@test.com");

        verify(avisRepository).deleteById("enfant1");
        verify(avisRepository).deleteById("avis1");
    }

    @Test
    @DisplayName("deleteAvis : autre utilisateur → exception 403")
    void deleteAvis_parAutreUtilisateur_lanceException403() {
        SignupEntity autreUser = SignupEntity.builder().id("user2").email("autre@test.com").build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("autre@test.com")).thenReturn(Optional.of(autreUser));

        assertThatThrownBy(() -> avisService.deleteAvis("avis1", "autre@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(avisRepository, never()).deleteById(anyString());
    }

    // =========================================================
    // validerAvis / rejeterAvis
    // =========================================================

    @Test
    @DisplayName("validerAvis : admin valide un avis → statut VALIDE et valide=true")
    void validerAvis_parAdmin_metStatutValide() {
        Avis avisValide = Avis.builder().id("avis1").note(4).commentaire("Excellent site")
                .utilisateurId("user1").cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.VALIDE).valide(true).datePublication(new Date()).build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(avisRepository.save(any(Avis.class))).thenReturn(avisValide);
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        AvisResponseDTO result = avisService.validerAvis("avis1", "admin@test.com");

        assertThat(result.isValide()).isTrue();
        assertThat(result.getStatut()).isEqualTo(StatutAvis.VALIDE);
        verify(avisRepository).save(argThat(a -> a.getStatut() == StatutAvis.VALIDE && a.isValide()));
    }

    @Test
    @DisplayName("validerAvis : avis inexistant → exception 404")
    void validerAvis_avisInexistant_lanceException404() {
        when(avisRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avisService.validerAvis("inexistant", "admin@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("rejeterAvis : admin rejette avec motif → statut REJETE et valide=false")
    void rejeterAvis_avecMotif_metStatutRejete() {
        Avis avisRejete = Avis.builder().id("avis1").note(4).commentaire("Excellent site")
                .utilisateurId("user1").cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.REJETE).valide(false).motifRejet("Contenu inapproprié")
                .datePublication(new Date()).build();

        when(avisRepository.findById("avis1")).thenReturn(Optional.of(avis));
        when(signupRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(avisRepository.save(any(Avis.class))).thenReturn(avisRejete);
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        AvisResponseDTO result = avisService.rejeterAvis("avis1", "Contenu inapproprié", "admin@test.com");

        assertThat(result.isValide()).isFalse();
        assertThat(result.getStatut()).isEqualTo(StatutAvis.REJETE);
        verify(avisRepository).save(argThat(a ->
                a.getStatut() == StatutAvis.REJETE
                && !a.isValide()
                && "Contenu inapproprié".equals(a.getMotifRejet())));
    }

    // =========================================================
    // getAvisValides
    // =========================================================

    @Test
    @DisplayName("getAvisValides : retourne uniquement les avis validés sans parent")
    void getAvisValides_retourneAvisValides() {
        Avis avisValide = Avis.builder().id("av1").note(5).commentaire("Super")
                .utilisateurId("user1").statut(StatutAvis.VALIDE).valide(true).datePublication(new Date()).build();

        when(avisRepository.findByStatutAndParentAvisIdIsNullOrderByDatePublicationDesc(StatutAvis.VALIDE))
                .thenReturn(List.of(avisValide));
        when(signupRepository.findById("user1")).thenReturn(Optional.of(utilisateur));

        List<AvisResponseDTO> result = avisService.getAvisValides();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isValide()).isTrue();
    }

    // =========================================================
    // getStatistiquesAvis
    // =========================================================

    @Test
    @DisplayName("getStatistiquesAvis : retourne les statistiques des avis valides")
    void getStatistiquesAvis_retourneStatistiquesCalculees() {
        Avis av1 = Avis.builder().id("a1").note(5).commentaire("Super").utilisateurId("user1")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING).valide(true)
                .statut(StatutAvis.VALIDE).datePublication(new Date()).build();
        Avis av2 = Avis.builder().id("a2").note(3).commentaire("Moyen").utilisateurId("user1")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING).valide(true)
                .statut(StatutAvis.VALIDE).datePublication(new Date()).build();
        Avis avNonValide = Avis.builder().id("a3").note(1).commentaire("Nul").utilisateurId("user1")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING).valide(false)
                .statut(StatutAvis.EN_ATTENTE).datePublication(new Date()).build();

        when(avisRepository.findByCibleIdAndTypeCible("site1", TypeCible.SITE_CAMPING))
                .thenReturn(List.of(av1, av2, avNonValide));

        StatistiquesAvisDTO stats = avisService.getStatistiquesAvis("site1", "SITE_CAMPING");

        assertThat(stats).isNotNull();
        // Seuls av1 (note=5) et av2 (note=3) sont valides → moyenne = 4.0
        assertThat(stats.getNoteMoyenne()).isEqualTo(4.0);
        assertThat(stats.getNombreTotal()).isEqualTo(2);
    }

    // =========================================================
    // getAvisAmis
    // =========================================================

    @Test
    @DisplayName("getAvisAmis : utilisateur sans abonnements → liste vide")
    void getAvisAmis_sansAbonnements_retourneListeVide() {
        when(abonnementRepository.findBySuiveurId("user@test.com")).thenReturn(List.of());

        List<AvisResponseDTO> result = avisService.getAvisAmis("user@test.com");

        assertThat(result).isEmpty();
        verifyNoInteractions(avisRepository);
    }

    @Test
    @DisplayName("getAvisAmis : avec abonnements → retourne avis des suivis")
    void getAvisAmis_avecAbonnements_retourneAvisDesSuivis() {
        Abonnement abonnement = Abonnement.builder()
                .suiveurId("user@test.com").suiviId("ami@test.com").build();

        SignupEntity ami = SignupEntity.builder()
                .id("ami1").email("ami@test.com").firstName("Marie").lastName("Martin").build();

        Avis avisAmi = Avis.builder().id("avisAmi").note(4).commentaire("Cool")
                .utilisateurId("ami1").cibleId("site2").typeCible(TypeCible.PRODUIT)
                .valide(true).statut(StatutAvis.VALIDE).datePublication(new Date()).build();

        when(abonnementRepository.findBySuiveurId("user@test.com")).thenReturn(List.of(abonnement));
        when(signupRepository.findByEmail("ami@test.com")).thenReturn(Optional.of(ami));
        when(avisRepository.findByUtilisateurIdInAndValideOrderByDatePublicationDesc(
                List.of("ami1"), true)).thenReturn(List.of(avisAmi));
        when(signupRepository.findById("ami1")).thenReturn(Optional.of(ami));

        List<AvisResponseDTO> result = avisService.getAvisAmis("user@test.com");

        assertThat(result).hasSize(1);
    }
}
