package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tests integration complete - Avis social")
class AvisSocialFullIntegrationTest extends SocialIntegrationTestSupport {

    @Test
    @DisplayName("Avis social : creation, moderation admin, consultation et statistiques en base")
    void avisSocial_parcoursComplet() throws Exception {
        MvcResult avisResult = mockMvc.perform(post("/api/avis")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "note", 5,
                                "commentaire", "Excellent camping social",
                                "cibleId", "site-social-1",
                                "typeCible", TypeCible.SITE_CAMPING.name()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value(StatutAvis.EN_ATTENTE.name()))
                .andReturn();

        String avisId = readJson(avisResult).get("id").asText();
        Avis avis = avisRepository.findById(avisId).orElseThrow();
        assertThat(avis.getUtilisateurId()).isEqualTo(aliceUser.getId());
        assertThat(avis.isValide()).isFalse();

        mockMvc.perform(post("/api/avis/{id}/valider", avisId)
                        .with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(StatutAvis.VALIDE.name()));

        Avis validated = avisRepository.findById(avisId).orElseThrow();
        assertThat(validated.isValide()).isTrue();
        assertThat(validated.getAdminId()).isEqualTo(adminUser.getId());

        mockMvc.perform(get("/api/avis/cible/{cibleId}", "site-social-1")
                        .param("typeCible", TypeCible.SITE_CAMPING.name())
                        .with(user(BOB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avisId));

        mockMvc.perform(get("/api/avis/statistiques/{cibleId}", "site-social-1")
                        .param("typeCible", TypeCible.SITE_CAMPING.name())
                        .with(user(BOB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreTotal").value(1))
                .andExpect(jsonPath("$.noteMoyenne").value(5.0));
    }
}
