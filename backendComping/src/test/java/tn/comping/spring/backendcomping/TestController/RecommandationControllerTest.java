package tn.comping.spring.backendcomping.TestController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.RecommandationController;
import tn.comping.spring.backendcomping.dto.SortieScoreDTO;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.services.serviceImpl.IRecommandationService;

@WebMvcTest(RecommandationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommandationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IRecommandationService recommandationService;

    private Sortie sortie;

    @BeforeEach
    void setUp() {
        sortie = Sortie.builder().id("S1").titre("Sortie test").build();
    }

    @Test
    void shouldReturnRecommendedSorties() throws Exception {
        when(recommandationService.recommanderSorties("U1"))
                .thenReturn(List.of(new SortieScoreDTO(sortie, 0.75, "Raison préférée")));

        mockMvc.perform(get("/api/recommandations/sorties")
                        .param("userId", "U1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sortie.id").value("S1"));

        verify(recommandationService).recommanderSorties("U1");
    }

    @Test
    void shouldUpdateProfilAfterParticipation() throws Exception {
        Map<String, String> body = Map.of("userId", "U1");

        mockMvc.perform(post("/api/recommandations/participation/mise-a-jour")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profil mis à jour"));

        verify(recommandationService).mettreAJourProfilApresInscription("U1");
    }
}
