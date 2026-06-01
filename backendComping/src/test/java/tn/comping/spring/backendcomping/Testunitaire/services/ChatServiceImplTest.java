package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.entities.Message;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.ConversationRepository;
import tn.comping.spring.backendcomping.repositories.MessageRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.ChatServiceImpl;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires - ChatServiceImpl")
class ChatServiceImplTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private SignupRepository signupRepository;
    @Mock private SimpUserRegistry userRegistry;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private ChatServiceImpl chatService;

    private SignupEntity user1;
    private SignupEntity user2;
    private Conversation conversation1to1;
    private Message message;

    @BeforeEach
    void setUp() {
        user1 = SignupEntity.builder()
                .id("id1").email("user1@test.com")
                .firstName("Alice").lastName("Martin").build();

        user2 = SignupEntity.builder()
                .id("id2").email("user2@test.com")
                .firstName("Bob").lastName("Dupont").build();

        // participant1Id = le plus petit lexicographiquement
        String p1 = "user1@test.com".compareTo("user2@test.com") < 0 ? "user1@test.com" : "user2@test.com";
        String p2 = "user1@test.com".compareTo("user2@test.com") < 0 ? "user2@test.com" : "user1@test.com";

        conversation1to1 = Conversation.builder()
                .id("conv1")
                .participant1Id(p1)
                .participant2Id(p2)
                .groupe(false)
                .messagesNonLusP1(0)
                .messagesNonLusP2(0)
                .dateDernierMessage(new Date())
                .dateCreation(new Date())
                .build();

        message = Message.builder()
                .id("msg1")
                .conversationId("conv1")
                .expediteurId("user1@test.com")
                .destinataireId("user2@test.com")
                .contenu("Bonjour!")
                .typeMessage("TEXT")
                .lu(false)
                .dateCreation(new Date())
                .build();
    }

    // =========================================================
    // getOrCreateConversation
    // =========================================================

    @Test
    @DisplayName("getOrCreateConversation : conversation déjà existante → retourne l'existante")
    void getOrCreateConversation_existante_retourneExistante() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user2@test.com");

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(conversationRepository.findByParticipant1IdAndParticipant2Id(anyString(), anyString()))
                .thenReturn(Optional.of(conversation1to1));

        ConversationResponseDTO result = chatService.getOrCreateConversation("user1@test.com", dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("conv1");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrCreateConversation : nouvelle conversation → créée et sauvegardée")
    void getOrCreateConversation_nouvelle_creeeEtSauvegardee() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user2@test.com");

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(conversationRepository.findByParticipant1IdAndParticipant2Id(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation1to1);

        ConversationResponseDTO result = chatService.getOrCreateConversation("user1@test.com", dto);

        assertThat(result).isNotNull();
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("getOrCreateConversation : même utilisateur → exception 400")
    void getOrCreateConversation_memeUtilisateur_lanceException400() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user1@test.com");

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));

        assertThatThrownBy(() -> chatService.getOrCreateConversation("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("getOrCreateConversation : destinataire null → exception 400")
    void getOrCreateConversation_destinataireNull_lanceException400() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id(null);

        assertThatThrownBy(() -> chatService.getOrCreateConversation("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("getOrCreateConversation : utilisateur introuvable → exception 404")
    void getOrCreateConversation_utilisateurInexistant_lanceException404() {
        ConversationRequestDTO dto = new ConversationRequestDTO();
        dto.setParticipant2Id("user2@test.com");

        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());
        when(signupRepository.findById("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getOrCreateConversation("inconnu@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // createGroupConversation
    // =========================================================

    @Test
    @DisplayName("createGroupConversation : moins de 2 participants → exception 400")
    void createGroupConversation_moinsDe2Participants_lanceException400() {
        GroupConversationRequestDTO dto = new GroupConversationRequestDTO();
        dto.setParticipantIds(List.of("user2@test.com")); // seulement 1 participant en plus du créateur

        // 1 participant dans la liste = 2 total (créateur + 1), mais le service exige min 2 dans la liste
        // La règle: dto.getParticipantIds().size() < 2
        dto.setParticipantIds(List.of("user2@test.com")); // size = 1 → exception

        assertThatThrownBy(() -> chatService.createGroupConversation("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("createGroupConversation : avec nom et participants → groupe créé")
    void createGroupConversation_parametresValides_creeeGroupe() {
        SignupEntity user3 = SignupEntity.builder()
                .id("id3").email("user3@test.com").firstName("Charlie").lastName("K").build();

        GroupConversationRequestDTO dto = new GroupConversationRequestDTO();
        dto.setNomGroupe("Camping Friends");
        dto.setParticipantIds(List.of("user2@test.com", "user3@test.com"));

        Conversation groupe = Conversation.builder()
                .id("grp1").groupe(true).nomGroupe("Camping Friends")
                .participantIds(List.of("user1@test.com", "user2@test.com", "user3@test.com"))
                .createurId("user1@test.com")
                .messagesNonLusParParticipant(new HashMap<>())
                .dateCreation(new Date()).dateDernierMessage(new Date()).build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findByEmail("user3@test.com")).thenReturn(Optional.of(user3));
        when(signupRepository.findById("user3@test.com")).thenReturn(Optional.of(user3));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(groupe);

        ConversationResponseDTO result = chatService.createGroupConversation("user1@test.com", dto);

        assertThat(result).isNotNull();
        assertThat(result.isGroupe()).isTrue();
        assertThat(result.getNomGroupe()).isEqualTo("Camping Friends");
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("createGroupConversation : sans nom → nom par défaut appliqué")
    void createGroupConversation_sansNom_appliqueNomParDefaut() {
        SignupEntity user3 = SignupEntity.builder()
                .id("id3").email("user3@test.com").firstName("Charlie").lastName("K").build();

        GroupConversationRequestDTO dto = new GroupConversationRequestDTO();
        dto.setNomGroupe(null);
        dto.setParticipantIds(List.of("user2@test.com", "user3@test.com"));

        Conversation groupe = Conversation.builder()
                .id("grp1").groupe(true).nomGroupe("Groupe camping")
                .participantIds(List.of("user1@test.com", "user2@test.com", "user3@test.com"))
                .createurId("user1@test.com")
                .messagesNonLusParParticipant(new HashMap<>())
                .dateCreation(new Date()).dateDernierMessage(new Date()).build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findByEmail("user3@test.com")).thenReturn(Optional.of(user3));
        when(signupRepository.findById("user3@test.com")).thenReturn(Optional.of(user3));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(groupe);

        ConversationResponseDTO result = chatService.createGroupConversation("user1@test.com", dto);

        assertThat(result.getNomGroupe()).isEqualTo("Groupe camping");
    }

    // =========================================================
    // sendMessage
    // =========================================================

    @Test
    @DisplayName("sendMessage : message valide 1:1 → sauvegardé et diffusé via WebSocket")
    void sendMessage_messageValide_sauvegardeEtDiffuse() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("conv1").contenu("Bonjour!").typeMessage("TEXT").build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation1to1);
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));

        MessageResponseDTO result = chatService.sendMessage("user1@test.com", dto);

        assertThat(result).isNotNull();
        assertThat(result.getContenu()).isEqualTo("Bonjour!");
        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/conv1"), any(MessageResponseDTO.class));
    }

    @Test
    @DisplayName("sendMessage : contenu vide → exception 400")
    void sendMessage_contenuVide_lanceException400() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("conv1").contenu("   ").build();

        assertThatThrownBy(() -> chatService.sendMessage("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("sendMessage : conversationId null → exception 400")
    void sendMessage_conversationIdNull_lanceException400() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId(null).contenu("Bonjour").build();

        assertThatThrownBy(() -> chatService.sendMessage("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("sendMessage : utilisateur non participant → exception 403")
    void sendMessage_nonParticipant_lanceException403() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("conv1").contenu("Je m'invite!").build();

        SignupEntity intrus = SignupEntity.builder()
                .id("id99").email("intrus@test.com").firstName("X").lastName("Y").build();

        when(signupRepository.findByEmail("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(signupRepository.findById("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.sendMessage("intrus@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage : conversation inexistante → exception 404")
    void sendMessage_conversationInexistante_lanceException404() {
        MessageRequestDTO dto = MessageRequestDTO.builder()
                .conversationId("inexistant").contenu("Bonjour").build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.sendMessage("user1@test.com", dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // updateMessage
    // =========================================================

    @Test
    @DisplayName("updateMessage : expéditeur modifie dans la fenêtre de 10 min → message mis à jour")
    void updateMessage_parExpediteurDansDelai_retourneMisAJour() {
        Message msgRecent = Message.builder()
                .id("msg1").conversationId("conv1")
                .expediteurId("user1@test.com").destinataireId("user2@test.com")
                .contenu("Bonjour!").typeMessage("TEXT").lu(false)
                .dateCreation(new Date()).build();

        Message msgMaj = Message.builder()
                .id("msg1").conversationId("conv1")
                .expediteurId("user1@test.com").destinataireId("user2@test.com")
                .contenu("Bonjour modifié!").typeMessage("TEXT").lu(false)
                .dateCreation(new Date()).build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(messageRepository.findById("msg1")).thenReturn(Optional.of(msgRecent));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));
        when(messageRepository.save(any(Message.class))).thenReturn(msgMaj);
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));

        MessageResponseDTO result = chatService.updateMessage("msg1", "Bonjour modifié!", "user1@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getContenu()).isEqualTo("Bonjour modifié!");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("updateMessage : autre utilisateur → exception 403")
    void updateMessage_parAutreUtilisateur_lanceException403() {
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(messageRepository.findById("msg1")).thenReturn(Optional.of(message));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.updateMessage("msg1", "Modification", "user2@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("updateMessage : message trop ancien (> 10 min) → exception 403")
    void updateMessage_messageTropAncien_lanceException403() {
        Message msgAncien = Message.builder()
                .id("msg1").conversationId("conv1")
                .expediteurId("user1@test.com").destinataireId("user2@test.com")
                .contenu("Vieux message").typeMessage("TEXT").lu(false)
                .dateCreation(new Date(System.currentTimeMillis() - 11 * 60 * 1000L)).build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(messageRepository.findById("msg1")).thenReturn(Optional.of(msgAncien));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.updateMessage("msg1", "Modification tardive", "user1@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("updateMessage : message de type VOICE → exception 400")
    void updateMessage_messageVoice_lanceException400() {
        Message msgVoice = Message.builder()
                .id("msg1").conversationId("conv1")
                .expediteurId("user1@test.com").destinataireId("user2@test.com")
                .contenu("/uploads/voice/audio.wav").typeMessage("VOICE").lu(false)
                .dateCreation(new Date()).build();

        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(messageRepository.findById("msg1")).thenReturn(Optional.of(msgVoice));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.updateMessage("msg1", "Texte", "user1@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("updateMessage : contenu vide → exception 400")
    void updateMessage_contenuVide_lanceException400() {
        assertThatThrownBy(() -> chatService.updateMessage("msg1", "   ", "user1@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // =========================================================
    // deleteMessage
    // =========================================================

    @Test
    @DisplayName("deleteMessage : expéditeur supprime dans le délai → message supprimé")
    void deleteMessage_parExpediteurDansDelai_supprime() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(messageRepository.findById("msg1")).thenReturn(Optional.of(message));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        chatService.deleteMessage("msg1", "user1@test.com");

        verify(messageRepository).delete(message);
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/conv1"), any(Map.class));
    }

    @Test
    @DisplayName("deleteMessage : message inexistant → exception 404")
    void deleteMessage_messageInexistant_lanceException404() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(messageRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.deleteMessage("inexistant", "user1@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // deleteConversation
    // =========================================================

    @Test
    @DisplayName("deleteConversation : participant supprime → messages et conversation supprimés")
    void deleteConversation_parParticipant_supprimeTout() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        chatService.deleteConversation("conv1", "user1@test.com");

        verify(messageRepository).deleteByConversationId("conv1");
        verify(conversationRepository).deleteById("conv1");
        verify(messagingTemplate).convertAndSend(eq("/topic/conversations/conv1"), any(Map.class));
    }

    @Test
    @DisplayName("deleteConversation : non participant → exception 403")
    void deleteConversation_nonParticipant_lanceException403() {
        SignupEntity intrus = SignupEntity.builder()
                .id("id99").email("intrus@test.com").firstName("X").lastName("Y").build();

        when(signupRepository.findByEmail("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(signupRepository.findById("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.deleteConversation("conv1", "intrus@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(messageRepository, never()).deleteByConversationId(any());
        verify(conversationRepository, never()).deleteById(any());
    }

    // =========================================================
    // markAsRead
    // =========================================================

    @Test
    @DisplayName("markAsRead : réinitialise compteur messages non lus pour participant 2")
    void markAsRead_resetCompteurNonLus() {
        conversation1to1.setMessagesNonLusP2(3);

        Message msgNonLu = Message.builder()
                .id("m1").conversationId("conv1")
                .expediteurId("user1@test.com").destinataireId("user2@test.com")
                .contenu("Hey").typeMessage("TEXT").lu(false).dateCreation(new Date()).build();

        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));
        when(messageRepository.findByConversationIdOrderByDateCreationAsc("conv1"))
                .thenReturn(List.of(msgNonLu));
        when(messageRepository.save(any(Message.class))).thenReturn(msgNonLu);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation1to1);

        chatService.markAsRead("conv1", "user2@test.com");

        verify(conversationRepository).save(argThat(c -> c.getMessagesNonLusP2() == 0));
        verify(messageRepository).save(argThat(m -> m.isLu()));
    }

    // =========================================================
    // getUserConversations
    // =========================================================

    @Test
    @DisplayName("getUserConversations : retourne les conversations 1:1 et les groupes de l'utilisateur")
    void getUserConversations_retourneConversations() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(
                "user1@test.com", "user1@test.com"))
                .thenReturn(List.of(conversation1to1));
        when(conversationRepository.findAll()).thenReturn(List.of()); // pas de groupes
        when(signupRepository.findByEmail("user2@test.com")).thenReturn(Optional.of(user2));
        when(signupRepository.findById("user2@test.com")).thenReturn(Optional.of(user2));

        List<ConversationResponseDTO> result = chatService.getUserConversations("user1@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("conv1");
    }

    // =========================================================
    // getMessages
    // =========================================================

    @Test
    @DisplayName("getMessages : participant valide → liste des messages paginée")
    void getMessages_participantValide_retourneMessages() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));
        when(messageRepository.findByConversationIdOrderByDateCreationAsc(
                eq("conv1"), any(Pageable.class))).thenReturn(List.of(message));
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));

        List<MessageResponseDTO> result = chatService.getMessages("conv1", 0, 50, "user1@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContenu()).isEqualTo("Bonjour!");
    }

    @Test
    @DisplayName("getMessages : non participant → exception 403")
    void getMessages_nonParticipant_lanceException403() {
        SignupEntity intrus = SignupEntity.builder()
                .id("id99").email("intrus@test.com").firstName("X").lastName("Y").build();

        when(signupRepository.findByEmail("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(signupRepository.findById("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.getMessages("conv1", 0, 50, "intrus@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    // =========================================================
    // isUserOnline / getUserStatus
    // =========================================================

    @Test
    @DisplayName("isUserOnline : utilisateur non connecté → retourne false")
    void isUserOnline_utilisateurDeconnecte_retourneFalse() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));

        boolean online = chatService.isUserOnline("user1@test.com");

        assertThat(online).isFalse();
    }

    @Test
    @DisplayName("getUserStatus : utilisateur inexistant → statut avec clé originale")
    void getUserStatus_utilisateurInexistant_retourneStatutPartiel() {
        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());
        when(signupRepository.findById("inconnu@test.com")).thenReturn(Optional.empty());

        UserStatusDTO status = chatService.getUserStatus("inconnu@test.com");

        assertThat(status).isNotNull();
        assertThat(status.getUserId()).isEqualTo("inconnu@test.com");
        assertThat(status.isOnline()).isFalse();
    }

    // =========================================================
    // addParticipantToGroup
    // =========================================================

    @Test
    @DisplayName("addParticipantToGroup : conversation non-groupe → exception 400")
    void addParticipantToGroup_convNonGroupe_lanceException400() {
        when(signupRepository.findByEmail("user1@test.com")).thenReturn(Optional.of(user1));
        when(signupRepository.findById("user1@test.com")).thenReturn(Optional.of(user1));
        when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation1to1));

        assertThatThrownBy(() -> chatService.addParticipantToGroup("conv1", "user1@test.com", "user2@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("addParticipantToGroup : non membre tente d'ajouter → exception 403")
    void addParticipantToGroup_nonMembre_lanceException403() {
        SignupEntity intrus = SignupEntity.builder()
                .id("id99").email("intrus@test.com").firstName("X").lastName("Y").build();

        Conversation groupe = Conversation.builder()
                .id("grp1").groupe(true).nomGroupe("Test")
                .participantIds(new ArrayList<>(List.of("user1@test.com", "user2@test.com")))
                .createurId("user1@test.com").messagesNonLusParParticipant(new HashMap<>())
                .dateCreation(new Date()).dateDernierMessage(new Date()).build();

        when(signupRepository.findByEmail("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(signupRepository.findById("intrus@test.com")).thenReturn(Optional.of(intrus));
        when(conversationRepository.findById("grp1")).thenReturn(Optional.of(groupe));

        assertThatThrownBy(() -> chatService.addParticipantToGroup("grp1", "intrus@test.com", "user3@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }
}
