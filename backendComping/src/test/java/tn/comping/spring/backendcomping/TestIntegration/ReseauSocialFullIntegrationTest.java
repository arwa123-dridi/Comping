package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.comping.spring.backendcomping.entities.Commentaire;
import tn.comping.spring.backendcomping.entities.Interaction;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tests integration complete - Reseau social")
class ReseauSocialFullIntegrationTest extends SocialIntegrationTestSupport {

    @Test
    @DisplayName("Posts/commentaires/reactions : API -> services sociaux -> repositories -> Mongo")
    void postsCommentairesReactions_parcoursComplet() throws Exception {
        String postId = createPost(ALICE, "Premier post #camping", "PUBLIC");

        assertThat(postRepository.findById(postId)).isPresent()
                .get()
                .satisfies(post -> {
                    assertThat(post.getAuteurId()).isEqualTo(ALICE);
                    assertThat(post.getHashtags()).contains("camping");
                    assertThat(post.getCommentairesCount()).isZero();
                });

        mockMvc.perform(post("/api/posts/{id}/react", postId)
                        .with(user(BOB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("emoji", "STAR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId));

        Interaction reaction = interactionRepository
                .findByAuteurIdAndCibleTypeAndCibleIdAndType(BOB, "POST", postId, "REACTION")
                .orElseThrow();
        assertThat(reaction.getEmoji()).isEqualTo("STAR");
        assertThat(postRepository.findById(postId).orElseThrow().getReactions()).containsEntry("STAR", 1);

        MvcResult commentResult = mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .with(user(BOB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("contenu", "Super idee de sortie"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").value("Super idee de sortie"))
                .andReturn();

        String commentId = readJson(commentResult).get("id").asText();
        Commentaire savedComment = commentaireRepository.findById(commentId).orElseThrow();
        assertThat(savedComment.getPostId()).isEqualTo(postId);
        assertThat(savedComment.getAuteurId()).isEqualTo(BOB);
        assertThat(postRepository.findById(postId).orElseThrow().getCommentairesCount()).isEqualTo(1);

        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId));
    }

    @Test
    @DisplayName("Abonnements/feed amis : suivre un campeur rend ses posts amis visibles")
    void abonnementsEtFeedAmis_parcoursComplet() throws Exception {
        String bobFriendsPostId = createPost(BOB, "Post pour mes amis #amis", "AMIS");
        createPost(CAROL, "Post prive invisible", "PRIVE");

        mockMvc.perform(post("/api/abonnements/suivre")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("suiviId", BOB))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suiviId").value(BOB));

        assertThat(abonnementRepository.existsBySuiveurIdAndSuiviId(ALICE, BOB)).isTrue();

        mockMvc.perform(get("/api/posts/amis")
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bobFriendsPostId));

        mockMvc.perform(get("/api/abonnements/stats/{userId}", BOB)
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.followers").value(1));

        mockMvc.perform(delete("/api/abonnements/retirer/{suiviId}", BOB)
                        .with(user(ALICE)))
                .andExpect(status().isNoContent());

        assertThat(abonnementRepository.existsBySuiveurIdAndSuiviId(ALICE, BOB)).isFalse();
    }

    private String createPost(String username, String contenu, String visibilite) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .with(user(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "contenu", contenu,
                                "visibilite", visibilite))))
                .andExpect(status().isOk())
                .andReturn();
        return readJson(result).get("id").asText();
    }
}
