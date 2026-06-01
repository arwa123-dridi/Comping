package tn.comping.spring.backendcomping.TestIntegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "testuser", roles = "USER")
class ActivityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void setup() {
        activityRepository.deleteAll();
    }

    // ================= CREATE =================
    @Test
    void shouldCreateActivity() throws Exception {

        ActivityRequest request = new ActivityRequest();
        request.setNom("Escalade");
        request.setDescription("Activité d'escalade en plein air");
        request.setType("Sport");
        request.setDuree("2h");
        request.setCapacite("15");
        request.setNiveauDifficulte("Intermédiaire");
        request.setTrancheAge("18-35");
        request.setSaison("Été");
        request.setTags(List.of("sport", "outdoor"));

        mockMvc.perform(post("/api/activities/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Escalade"));
    }

    // ================= GET ALL =================
    @Test
    void shouldGetAllActivities() throws Exception {

        Activity activity = Activity.builder()
                .nom("Randonnée")
                .description("Randonnée en montagne")
                .type("Outdoor")
                .duree("3h")
                .capacite("20")
                .niveauDifficulte("Facile")
                .trancheAge("12+")
                .saison("Printemps")
                .tags(List.of("nature", "marche"))
                .build();

        activityRepository.save(activity);

        mockMvc.perform(get("/api/activities/GetAllActivities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Randonnée"));
    }

    // ================= GET BY ID =================
    @Test
    void shouldGetActivityById() throws Exception {

        Activity activity = Activity.builder()
                .nom("Natation")
                .description("Natation en piscine")
                .type("Sport")
                .duree("1h")
                .capacite("10")
                .niveauDifficulte("Facile")
                .trancheAge("Tous âges")
                .saison("Toutes saisons")
                .tags(List.of("eau", "sport"))
                .build();

        Activity saved = activityRepository.save(activity);

        mockMvc.perform(get("/api/activities/" + saved.getIdActivity()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Natation"));
    }

    // ================= UPDATE =================
    @Test
    void shouldUpdateActivity() throws Exception {

        Activity activity = Activity.builder()
                .nom("Yoga")
                .description("Yoga débutant")
                .type("Bien-être")
                .duree("1h30")
                .capacite("12")
                .niveauDifficulte("Facile")
                .trancheAge("Tous âges")
                .saison("Toutes saisons")
                .tags(List.of("relaxation"))
                .build();

        Activity saved = activityRepository.save(activity);

        ActivityRequest updateRequest = new ActivityRequest();
        updateRequest.setNom("Yoga Avancé");
        updateRequest.setDescription("Yoga niveau avancé");
        updateRequest.setType("Bien-être");
        updateRequest.setDuree("2h");
        updateRequest.setCapacite("8");
        updateRequest.setNiveauDifficulte("Avancé");
        updateRequest.setTrancheAge("18+");
        updateRequest.setSaison("Toutes saisons");
        updateRequest.setTags(List.of("relaxation", "avancé"));

        mockMvc.perform(put("/api/activities/updateactivity/" + saved.getIdActivity())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Yoga Avancé"))
                .andExpect(jsonPath("$.niveauDifficulte").value("Avancé"));
    }

    // ================= DELETE =================
    @Test
    void shouldDeleteActivity() throws Exception {

        Activity activity = Activity.builder()
                .nom("Vélo")
                .description("Balade à vélo")
                .type("Sport")
                .duree("2h")
                .capacite("10")
                .niveauDifficulte("Facile")
                .trancheAge("Tous âges")
                .saison("Été")
                .tags(List.of("vélo", "outdoor"))
                .build();

        Activity saved = activityRepository.save(activity);

        mockMvc.perform(delete("/api/activities/deleteactivity/" + saved.getIdActivity()))
                .andExpect(status().isOk());
    }
}