package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.UserProfileRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.PlanningServiceImpl;
import tn.comping.spring.backendcomping.services.serviceImpl.RecommandationServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock private SortieRepository sortieRepo;
    @Mock private ParticipationRepository partRepo;
    @Mock private UserProfileRepository profileRepo;
    @Mock private SignupRepository signupRepo;
    @Mock private RecommandationServiceImpl recommandationService;

    @InjectMocks
    private PlanningServiceImpl planningService;

    private Sortie sortie;
    private UserProfile profil;
    private SignupEntity utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = SignupEntity.builder()
                .id("U1")
                .firstName("Test")
                .lastName("User")
                .build();

        profil = UserProfile.builder()
                .id("U1-profile")
                .utilisateur(utilisateur)
                .regionsFrequentes(List.of("Nord"))
                .difficultesFrequentes(List.of("FACILE"))
                .saisonsPreferees(List.of("PRINTEMPS"))
                .joursPreferees(List.of("SATURDAY", "SUNDAY"))
                .nbParticipationsTotal(3)
                .build();

        sortie = Sortie.builder()
                .id("S1")
                .titre("Randonnée test")
                .region("Nord")
                .difficulte(Difficulte.FACILE)
                .capaciteMax(5)
                .participantIds(List.of())
                .dateDebut(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Test
    void shouldGeneratePlanningFromAvailableSortie() {
        when(recommandationService.construireOuMettreAJourProfil("U1"))
                .thenReturn(profil);

        when(partRepo.findByUtilisateurIdOrderByDateInscriptionDesc("U1"))
                .thenReturn(List.of());

        when(sortieRepo.findBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sortie));

        when(partRepo.existsByUtilisateurIdAndSortieId("U1", "S1"))
                .thenReturn(false);

        var result = planningService.genererPlanning("U1");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("S1", result.get(0).getSortie().getId());
        assertTrue(result.get(0).isEstMeilleurChoix());
        verify(recommandationService).construireOuMettreAJourProfil("U1");
    }

    @Test
    void shouldValidateSortieAndSaveParticipation() {
        when(sortieRepo.findById("S1")).thenReturn(Optional.of(sortie));
        when(partRepo.existsByUtilisateurIdAndSortieId("U1", "S1")).thenReturn(false);
        when(signupRepo.findById("U1")).thenReturn(Optional.of(utilisateur));
        when(partRepo.save(any(Participation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sortieRepo.save(any(Sortie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(recommandationService).mettreAJourProfilApresInscription("U1");

        planningService.validerSortie("U1", "S1");

        assertTrue(sortie.getParticipantIds().contains("U1"));
        verify(partRepo).save(any(Participation.class));
        verify(sortieRepo).save(sortie);
        verify(recommandationService).mettreAJourProfilApresInscription("U1");
    }
}