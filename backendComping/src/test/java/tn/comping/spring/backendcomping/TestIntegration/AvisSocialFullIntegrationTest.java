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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    @DisplayName("Avis social : modification auteur, rejet admin et suppression")
    void avisModificationRejetSuppression_parcoursComplet() throws Exception {
        MvcResult avisResult = mockMvc.perform(post("/api/avis")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "note", 3,
                                "commentaire", "Avis a completer",
                                "cibleId", "site-social-2",
                                "typeCible", TypeCible.SITE_CAMPING.name()))))
                .andExpect(status().isCreated())
                .andReturn();
        String avisId = readJson(avisResult).get("id").asText();

        mockMvc.perform(put("/api/avis/{id}", avisId)
                        .with(user(BOB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "note", 1,
                                "commentaire", "Modification interdite",
                                "cibleId", "site-social-2",
                                "typeCible", TypeCible.SITE_CAMPING.name()))))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/avis/{id}", avisId)
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "note", 4,
                                "commentaire", "Avis precise apres visite",
                                "cibleId", "site-social-2",
                                "typeCible", TypeCible.SITE_CAMPING.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(4))
                .andExpect(jsonPath("$.commentaire").value("Avis precise apres visite"));

        mockMvc.perform(post("/api/avis/{id}/rejeter", avisId)
                        .param("motif", "Preuve insuffisante")
                        .with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value(StatutAvis.REJETE.name()))
                .andExpect(jsonPath("$.valide").value(false));

        Avis rejected = avisRepository.findById(avisId).orElseThrow();
        assertThat(rejected.isValide()).isFalse();
        assertThat(rejected.getMotifRejet()).isEqualTo("Preuve insuffisante");
        assertThat(rejected.getAdminId()).isEqualTo(adminUser.getId());

        mockMvc.perform(get("/api/avis/statut/{statut}", StatutAvis.REJETE.name())
                        .with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avisId));

        mockMvc.perform(delete("/api/avis/{id}", avisId)
                        .with(user(ALICE)))
                .andExpect(status().isNoContent());

        assertThat(avisRepository.findById(avisId)).isEmpty();
    }

    @Test
    @DisplayName("Avis social : avis valides des amis via abonnement")
    void avisAmis_parcoursComplet() throws Exception {
        MvcResult avisBobResult = mockMvc.perform(post("/api/avis")
                        .with(user(BOB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "note", 5,
                                "commentaire", "Spot recommande aux amis",
                                "cibleId", "site-social-amis",
                                "typeCible", TypeCible.SITE_CAMPING.name()))))
                .andExpect(status().isCreated())
                .andReturn();
        String avisBobId = readJson(avisBobResult).get("id").asText();

        mockMvc.perform(post("/api/avis/{id}/valider", avisBobId)
                        .with(user(ADMIN).roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/avis/amis")
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mockMvc.perform(post("/api/abonnements/suivre")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("suiviId", BOB))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/avis/amis")
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avisBobId));

        mockMvc.perform(get("/api/avis/valides")
                        .with(user(CAROL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(avisBobId));
    }
}
