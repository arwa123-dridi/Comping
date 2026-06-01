package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.entities.Message;
import tn.comping.spring.backendcomping.entities.SignupEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tests integration complete - Chat social")
class ChatSocialFullIntegrationTest extends SocialIntegrationTestSupport {

    @Test
    @DisplayName("Chat : conversation, message, lecture et suppression traversent toute la stack")
    void chat_parcoursComplet() throws Exception {
        MvcResult conversationResult = mockMvc.perform(post("/api/chat/conversation")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("participant2Id", BOB))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupe").value(false))
                .andReturn();

        String conversationId = readJson(conversationResult).get("id").asText();
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow();
        assertThat(conversation.getParticipant1Id()).isIn(ALICE, BOB);
        assertThat(conversation.getParticipant2Id()).isIn(ALICE, BOB);

        MvcResult messageResult = mockMvc.perform(post("/api/chat/message")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "conversationId", conversationId,
                                "contenu", "Salut Bob",
                                "typeMessage", "TEXT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").value("Salut Bob"))
                .andReturn();

        String messageId = readJson(messageResult).get("id").asText();
        Message savedMessage = messageRepository.findById(messageId).orElseThrow();
        assertThat(savedMessage.getConversationId()).isEqualTo(conversationId);
        assertThat(savedMessage.getExpediteurId()).isEqualTo(ALICE);

        Conversation afterMessage = conversationRepository.findById(conversationId).orElseThrow();
        assertThat(afterMessage.getMessagesNonLusP1() + afterMessage.getMessagesNonLusP2()).isEqualTo(1);

        mockMvc.perform(put("/api/chat/messages/{conversationId}/read", conversationId)
                        .with(user(BOB)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chat/messages/{conversationId}", conversationId)
                        .with(user(BOB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(messageId));

        mockMvc.perform(delete("/api/chat/message/{messageId}", messageId)
                        .with(user(ALICE)))
                .andExpect(status().isNoContent());

        assertThat(messageRepository.findById(messageId)).isEmpty();
    }

    @Test
    @DisplayName("Chat groupe : creation, participants, message, edition, lecture et suppression conversation")
    void chatGroupe_parcoursComplet() throws Exception {
        MvcResult groupResult = mockMvc.perform(post("/api/chat/group")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nomGroupe", "Bivouac week-end",
                                "participantIds", java.util.List.of(BOB, CAROL)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupe").value(true))
                .andExpect(jsonPath("$.nomGroupe").value("Bivouac week-end"))
                .andReturn();

        String groupId = readJson(groupResult).get("id").asText();
        Conversation group = conversationRepository.findById(groupId).orElseThrow();
        assertThat(group.getParticipantIds()).contains(ALICE, BOB, CAROL);
        assertThat(group.getMessagesNonLusParParticipant().keySet())
                .contains(keyFor(group, ALICE), keyFor(group, BOB), keyFor(group, CAROL));

        SignupEntity dave = saveUser("Dave", "Social", "dave.social@test.com", tn.comping.spring.backendcomping.entities.Role.USER);

        mockMvc.perform(post("/api/chat/group/{conversationId}/add", groupId)
                        .param("participantId", dave.getEmail())
                        .with(user(BOB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds[?(@ == '" + dave.getEmail() + "')]").exists());

        MvcResult messageResult = mockMvc.perform(post("/api/chat/message")
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "conversationId", groupId,
                                "contenu", "Depart samedi matin",
                                "typeMessage", "TEXT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").value("Depart samedi matin"))
                .andReturn();

        String messageId = readJson(messageResult).get("id").asText();
        Conversation afterMessage = conversationRepository.findById(groupId).orElseThrow();
        assertThat(afterMessage.getMessagesNonLusParParticipant())
                .containsEntry(keyFor(afterMessage, BOB), 1)
                .containsEntry(keyFor(afterMessage, CAROL), 1)
                .containsEntry(keyFor(afterMessage, dave.getEmail()), 1);

        mockMvc.perform(put("/api/chat/message/{messageId}", messageId)
                        .with(user(ALICE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "conversationId", groupId,
                                "contenu", "Depart samedi a 8h",
                                "typeMessage", "TEXT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").value("Depart samedi a 8h"));

        mockMvc.perform(put("/api/chat/messages/{conversationId}/read", groupId)
                        .with(user(BOB)))
                .andExpect(status().isNoContent());

        assertThat(conversationRepository.findById(groupId).orElseThrow()
                .getMessagesNonLusParParticipant().get(keyFor(conversationRepository.findById(groupId).orElseThrow(), BOB))).isZero();

        mockMvc.perform(post("/api/chat/group/{conversationId}/remove", groupId)
                        .param("participantId", dave.getEmail())
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantIds[?(@ == '" + dave.getEmail() + "')]").doesNotExist());

        mockMvc.perform(get("/api/chat/conversations")
                        .with(user(CAROL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + groupId + "')]").exists());

        mockMvc.perform(delete("/api/chat/conversation/{conversationId}", groupId)
                        .with(user(ALICE)))
                .andExpect(status().isNoContent());

        assertThat(conversationRepository.findById(groupId)).isEmpty();
        assertThat(messageRepository.findByConversationIdOrderByDateCreationAsc(groupId)).isEmpty();
    }

    @Test
    @DisplayName("Chat statut : consultation du statut utilisateur et liste des connectes")
    void chatStatutUtilisateur_parcoursComplet() throws Exception {
        mockMvc.perform(get("/api/chat/status/{userId}", BOB)
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(BOB))
                .andExpect(jsonPath("$.online").value(false));

        mockMvc.perform(get("/api/chat/online")
                        .with(user(ALICE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private String keyFor(Conversation conversation, String email) {
        return conversation.getMessagesNonLusParParticipant().keySet().stream()
                .filter(key -> key.replaceAll("[^A-Za-z0-9@]", ".").equals(email))
                .findFirst()
                .orElseThrow();
    }
}
