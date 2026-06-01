package tn.comping.spring.backendcomping.TestController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.controllers.ChatController;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.services.ChatService;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires (Mockito pur) - ChatController")
class ChatControllerUnitTest {

    @Mock private ChatService chatService;

    @InjectMocks private ChatController chatController;

    private Authentication auth;
    private ConversationResponseDTO convResponse;
    private MessageResponseDTO msgResponse;

    @BeforeEach
    void setUp() {
        auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1@test.com");

        convResponse = ConversationResponseDTO.builder()
                .id("conv1").participant1Id("user1@test.com").participant2Id("user2@test.com")
                .participant1Nom("Alice").participant2Nom("Bob")
                .groupe(false).messagesNonLus(0).dateDernierMessage(new Date()).build();

        msgResponse = MessageResponseDTO.builder()
                .id("msg1").conversationId("conv1")
                .expediteurId("user1@test.com").expediteurNom("Alice")
                .contenu("Bonjour!").typeMessage("TEXT")
                .lu(false).dateCreation(new Date()).build();
    }

    // =========================================================
    // GET /api/chat/conversations
    // =========================================================

    @Test
    @DisplayName("getConversations : délègue au service et retourne liste")
    void getConversations_delegueAuService_retourneListe() {
        when(chatService.getUserConversations("user1@test.com"))
                .thenReturn(List.of(convResponse));

        ResponseEntity<List<ConversationResponseDTO>> response =
                chatController.getConversations(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(chatService).getUserConversations("user1@test.com");
    }

    @Test
    @DisplayName("getConversations : aucune conversation → liste vide")
    void getConversations_aucuneConversation_retourneListeVide() {
        when(chatService.getUserConversations("user1@test.com")).thenReturn(List.of());

        ResponseEntity<List<ConversationResponseDTO>> response =
                chatController.getConversations(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    // POST /api/chat/conversation
    // =========================================================

    @Test
    @DisplayName("getOrCreateConversation : crée ou récupère une conversation")
    void getOrCreateConversation_retourneConversation() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user2@test.com");
        when(chatService.getOrCreateConversation(eq("user1@test.com"), any()))
                .thenReturn(convResponse);

        ResponseEntity<ConversationResponseDTO> response =
                chatController.getOrCreateConversation(dto, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("conv1");
        verify(chatService).getOrCreateConversation(eq("user1@test.com"), any());
    }

    @Test
    @DisplayName("getOrCreateConversation : même utilisateur → 400 propagé")
    void getOrCreateConversation_memeUtilisateur_exception400Propagee() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user1@test.com");
        when(chatService.getOrCreateConversation(eq("user1@test.com"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Impossible de créer une conversation avec soi-même"));

        assertThatThrownBy(() -> chatController.getOrCreateConversation(dto, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // POST /api/chat/group
    // =========================================================

    @Test
    @DisplayName("createGroup : crée un groupe et retourne 200")
    void createGroup_retourneGroupe() {
        ConversationResponseDTO groupResponse = ConversationResponseDTO.builder()
                .id("grp1").groupe(true).nomGroupe("Camping Friends")
                .messagesNonLus(0).build();
        GroupConversationRequestDTO dto = new GroupConversationRequestDTO();
        dto.setNomGroupe("Camping Friends");
        dto.setParticipantIds(List.of("user2@test.com", "user3@test.com"));
        when(chatService.createGroupConversation(eq("user1@test.com"), any()))
                .thenReturn(groupResponse);

        ResponseEntity<ConversationResponseDTO> response = chatController.createGroup(dto, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isGroupe()).isTrue();
        assertThat(response.getBody().getNomGroupe()).isEqualTo("Camping Friends");
    }

    @Test
    @DisplayName("createGroup : participants insuffisants → 400 propagé")
    void createGroup_participantsInsuffisants_exception400Propagee() {
        GroupConversationRequestDTO dto = new GroupConversationRequestDTO();
        dto.setParticipantIds(List.of("user2@test.com"));
        when(chatService.createGroupConversation(eq("user1@test.com"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Au moins 2 participants requis"));

        assertThatThrownBy(() -> chatController.createGroup(dto, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // GET /api/chat/messages/{conversationId}
    // =========================================================

    @Test
    @DisplayName("getMessages : délègue au service avec page et size")
    void getMessages_delegueAvecParametres_retourneListe() {
        when(chatService.getMessages("conv1", 0, 50, "user1@test.com"))
                .thenReturn(List.of(msgResponse));

        ResponseEntity<List<MessageResponseDTO>> response =
                chatController.getMessages("conv1", 0, 50, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getContenu()).isEqualTo("Bonjour!");
        verify(chatService).getMessages("conv1", 0, 50, "user1@test.com");
    }

    @Test
    @DisplayName("getMessages : non participant → 403 propagé")
    void getMessages_nonParticipant_exception403Propagee() {
        when(chatService.getMessages("conv1", 0, 50, "user1@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Non participant"));

        assertThatThrownBy(() -> chatController.getMessages("conv1", 0, 50, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // POST /api/chat/message  →  sendMessage
    // =========================================================

    @Test
    @DisplayName("sendMessage : délègue au service et retourne message envoyé")
    void sendMessage_delegueAuService_retourneMessage() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("conv1").contenu("Bonjour!").typeMessage("TEXT").build();
        when(chatService.sendMessage(eq("user1@test.com"), any())).thenReturn(msgResponse);

        ResponseEntity<MessageResponseDTO> response = chatController.sendMessage(dto, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContenu()).isEqualTo("Bonjour!");
        verify(chatService).sendMessage(eq("user1@test.com"), any());
    }

    @Test
    @DisplayName("sendMessage : contenu vide → 400 propagé")
    void sendMessage_contenuVide_exception400Propagee() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("conv1").contenu("   ").build();
        when(chatService.sendMessage(eq("user1@test.com"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Message vide"));

        assertThatThrownBy(() -> chatController.sendMessage(dto, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // PUT /api/chat/message/{messageId}  →  updateMessage
    // =========================================================

    @Test
    @DisplayName("updateMessage : délègue au service avec contenu mis à jour")
    void updateMessage_delegueAuService_retourneMessageMisAJour() {
        MessageResponseDTO msgMaj = MessageResponseDTO.builder()
                .id("msg1").contenu("Bonjour modifié!").typeMessage("TEXT").build();
        MessageRequestDTO dto = MessageRequestDTO.builder().contenu("Bonjour modifié!").build();
        when(chatService.updateMessage("msg1", "Bonjour modifié!", "user1@test.com"))
                .thenReturn(msgMaj);

        ResponseEntity<MessageResponseDTO> response =
                chatController.updateMessage("msg1", dto, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContenu()).isEqualTo("Bonjour modifié!");
        verify(chatService).updateMessage("msg1", "Bonjour modifié!", "user1@test.com");
    }

    @Test
    @DisplayName("updateMessage : message trop ancien → 403 propagé")
    void updateMessage_messageTropAncien_exception403Propagee() {
        MessageRequestDTO dto = MessageRequestDTO.builder().contenu("Modification").build();
        when(chatService.updateMessage("msg1", "Modification", "user1@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Délai de 10 minutes dépassé"));

        assertThatThrownBy(() -> chatController.updateMessage("msg1", dto, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // DELETE /api/chat/message/{messageId}  →  deleteMessage
    // =========================================================

    @Test
    @DisplayName("deleteMessage : délègue au service et retourne 204")
    void deleteMessage_delegueAuService_retourne204() {
        doNothing().when(chatService).deleteMessage("msg1", "user1@test.com");

        ResponseEntity<Void> response = chatController.deleteMessage("msg1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chatService).deleteMessage("msg1", "user1@test.com");
    }

    // =========================================================
    // DELETE /api/chat/conversation/{conversationId}  →  deleteConversation
    // =========================================================

    @Test
    @DisplayName("deleteConversation : délègue au service et retourne 204")
    void deleteConversation_delegueAuService_retourne204() {
        doNothing().when(chatService).deleteConversation("conv1", "user1@test.com");

        ResponseEntity<Void> response = chatController.deleteConversation("conv1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chatService).deleteConversation("conv1", "user1@test.com");
    }

    @Test
    @DisplayName("deleteConversation : non participant → 403 propagé")
    void deleteConversation_nonParticipant_exception403Propagee() {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non participant"))
                .when(chatService).deleteConversation("conv1", "user1@test.com");

        assertThatThrownBy(() -> chatController.deleteConversation("conv1", auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // PUT /api/chat/messages/{conversationId}/read  →  markAsRead
    // =========================================================

    @Test
    @DisplayName("markAsRead : délègue au service et retourne 204")
    void markAsRead_delegueAuService_retourne204() {
        doNothing().when(chatService).markAsRead("conv1", "user1@test.com");

        ResponseEntity<Void> response = chatController.markAsRead("conv1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(chatService).markAsRead("conv1", "user1@test.com");
    }

    // =========================================================
    // GET /api/chat/status/{userId}  →  getUserStatus
    // =========================================================

    @Test
    @DisplayName("getUserStatus : délègue au service et retourne statut")
    void getUserStatus_delegueAuService_retourneStatut() {
        UserStatusDTO status = UserStatusDTO.builder()
                .userId("user2@test.com").nom("Bob Dupont").online(true).build();
        when(chatService.getUserStatus("user2@test.com")).thenReturn(status);

        ResponseEntity<UserStatusDTO> response = chatController.getUserStatus("user2@test.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isOnline()).isTrue();
        assertThat(response.getBody().getNom()).isEqualTo("Bob Dupont");
        verify(chatService).getUserStatus("user2@test.com");
    }

    // =========================================================
    // GET /api/chat/online  →  getOnlineUsers
    // =========================================================

    @Test
    @DisplayName("getOnlineUsers : délègue au service et retourne utilisateurs en ligne")
    void getOnlineUsers_delegueAuService_retourneListeEnLigne() {
        UserStatusDTO online = UserStatusDTO.builder()
                .userId("user1@test.com").online(true).build();
        when(chatService.getOnlineUsers()).thenReturn(List.of(online));

        ResponseEntity<List<UserStatusDTO>> response = chatController.getOnlineUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).isOnline()).isTrue();
        verify(chatService).getOnlineUsers();
    }

    // =========================================================
    // POST /api/chat/group/{conversationId}/add  →  addParticipant
    // =========================================================

    @Test
    @DisplayName("addParticipant : délègue au service et retourne groupe mis à jour")
    void addParticipant_delegueAuService_retourneGroupe() {
        ConversationResponseDTO groupMaj = ConversationResponseDTO.builder()
                .id("grp1").groupe(true).nomGroupe("Camping Friends").build();
        when(chatService.addParticipantToGroup("grp1", "user1@test.com", "user3@test.com"))
                .thenReturn(groupMaj);

        ResponseEntity<ConversationResponseDTO> response =
                chatController.addParticipant("grp1", "user3@test.com", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isGroupe()).isTrue();
        verify(chatService).addParticipantToGroup("grp1", "user1@test.com", "user3@test.com");
    }

    // =========================================================
    // POST /api/chat/group/{conversationId}/remove  →  removeParticipant
    // =========================================================

    @Test
    @DisplayName("removeParticipant : délègue au service et retourne groupe mis à jour")
    void removeParticipant_delegueAuService_retourneGroupe() {
        ConversationResponseDTO groupMaj = ConversationResponseDTO.builder()
                .id("grp1").groupe(true).nomGroupe("Camping Friends").build();
        when(chatService.removeParticipantFromGroup("grp1", "user1@test.com", "user2@test.com"))
                .thenReturn(groupMaj);

        ResponseEntity<ConversationResponseDTO> response =
                chatController.removeParticipant("grp1", "user2@test.com", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(chatService).removeParticipantFromGroup("grp1", "user1@test.com", "user2@test.com");
    }
}
