package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tn.comping.spring.backendcomping.entities.Difficulte;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.UserProfile;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.RecommandationServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RandonneeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SortieRepository sortieRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private SignupRepository signupRepository;

    @MockBean
    private RecommandationServiceImpl recommandationService;

    private SignupEntity utilisateur;
    private Sortie sortie;

    @BeforeEach
    void setUp() {
        participationRepository.deleteAll();
        sortieRepository.deleteAll();
        signupRepository.deleteAll();

        utilisateur = SignupEntity.builder()
                .id("integration-user")
                .firstName("Integration")
                .lastName("Tester")
                .build();
        signupRepository.save(utilisateur);

        sortie = Sortie.builder()
                .id("S1")
                .titre("Sortie test intégration")
                .region("Nord")
                .difficulte(Difficulte.FACILE)
                .capaciteMax(5)
                .participantIds(List.of())
                .dateDebut(LocalDateTime.now().plusDays(7))
                .build();
        sortieRepository.save(sortie);

        UserProfile profile = UserProfile.builder()
                .id("integration-user-profile")
                .utilisateur(utilisateur)
                .regionsFrequentes(List.of("Nord"))
                .difficultesFrequentes(List.of("FACILE"))
                .joursPreferees(List.of("SATURDAY", "SUNDAY"))
                .nbParticipationsTotal(1)
                .build();

        when(recommandationService.construireOuMettreAJourProfil("integration-user"))
                .thenReturn(profile);
        doNothing().when(recommandationService).mettreAJourProfilApresInscription("integration-user");
    }

    @Test
    void shouldReturnPlanningForIntegrationUser() throws Exception {
        mockMvc.perform(get("/api/planning/integration-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].sortie.id").value("S1"));
    }

    @Test
    void shouldValidateSortieAndPersistParticipant() throws Exception {
        mockMvc.perform(post("/api/planning/integration-user/valider")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sortieId\":\"S1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription confirmée avec succès"));

        Sortie updated = sortieRepository.findById("S1").orElseThrow();
        assertThat(updated.getParticipantIds().contains("integration-user"), is(true));
    }
}
