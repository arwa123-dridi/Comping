package tn.comping.spring.backendcomping.TestIntegration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.entities.Message;

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
}
