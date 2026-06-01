package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import tn.comping.spring.backendcomping.entities.Message;
import tn.comping.spring.backendcomping.repositories.MessageRepository;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - MessageRepository")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    private Message msg1;
    private Message msg2;
    private Message msg3LuParDest;

    @BeforeEach
    void setUp() {
        msg1 = messageRepository.save(Message.builder()
                .conversationId("conv1")
                .expediteurId("user1").destinataireId("user2")
                .contenu("Bonjour!").typeMessage("TEXT")
                .lu(false).dateCreation(new Date(System.currentTimeMillis() - 5000)).build());

        msg2 = messageRepository.save(Message.builder()
                .conversationId("conv1")
                .expediteurId("user2").destinataireId("user1")
                .contenu("Salut!").typeMessage("TEXT")
                .lu(false).dateCreation(new Date(System.currentTimeMillis() - 3000)).build());

        msg3LuParDest = messageRepository.save(Message.builder()
                .conversationId("conv1")
                .expediteurId("user1").destinataireId("user2")
                .contenu("Tu vas bien?").typeMessage("TEXT")
                .lu(true).dateCreation(new Date()).build());

        // Message dans une autre conversation
        messageRepository.save(Message.builder()
                .conversationId("conv2")
                .expediteurId("user3").destinataireId("user4")
                .contenu("Hello").typeMessage("TEXT")
                .lu(false).dateCreation(new Date()).build());
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
    }

    // =========================================================
    // findByConversationIdOrderByDateCreationAsc
    // =========================================================

    @Test
    @DisplayName("findByConversationId : retourne les messages d'une conversation ordonnés par date ASC")
    void findByConversationId_retourneMessagesOrdonnésAsc() {
        List<Message> result = messageRepository
                .findByConversationIdOrderByDateCreationAsc("conv1");

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(m -> "conv1".equals(m.getConversationId()));
        // Le plus ancien en premier
        assertThat(result.get(0).getId()).isEqualTo(msg1.getId());
        assertThat(result.get(2).getId()).isEqualTo(msg3LuParDest.getId());
    }

    @Test
    @DisplayName("findByConversationId : conversation inconnue → liste vide")
    void findByConversationId_convInconnue_retourneVide() {
        List<Message> result = messageRepository
                .findByConversationIdOrderByDateCreationAsc("conv_inconnue");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByConversationId avec pageable : limite le nombre de messages")
    void findByConversationId_avecPageable_limiteResultats() {
        List<Message> result = messageRepository
                .findByConversationIdOrderByDateCreationAsc("conv1", PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
    }

    // =========================================================
    // findByConversationIdAndExpediteurIdOrderByDateCreationDesc
    // =========================================================

    @Test
    @DisplayName("findByConversationIdAndExpediteurId : messages envoyés par un utilisateur")
    void findByConversationIdAndExpediteurId_retourneMessagesExpediteur() {
        List<Message> result = messageRepository
                .findByConversationIdAndExpediteurIdOrderByDateCreationDesc("conv1", "user1");

        assertThat(result).hasSize(2); // msg1 + msg3LuParDest
        assertThat(result).allMatch(m -> "user1".equals(m.getExpediteurId()));
    }

    @Test
    @DisplayName("findByConversationIdAndExpediteurId : utilisateur sans message dans la conv → vide")
    void findByConversationIdAndExpediteurId_sansMessage_retourneVide() {
        List<Message> result = messageRepository
                .findByConversationIdAndExpediteurIdOrderByDateCreationDesc("conv1", "user_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // countByConversationIdAndDestinataireIdAndLuFalse
    // =========================================================

    @Test
    @DisplayName("countByConversationIdAndDestinataireIdAndLuFalse : compte les messages non lus")
    void countByConversationIdAndDestinataireIdAndLuFalse_retourneNombre() {
        long count = messageRepository
                .countByConversationIdAndDestinataireIdAndLuFalse("conv1", "user2");

        assertThat(count).isEqualTo(1); // msg1 est non lu par user2 (msg3 est lu)
    }

    @Test
    @DisplayName("countByConversationIdAndDestinataireIdAndLuFalse : tous lus → 0")
    void countByConversationIdAndDestinataireIdAndLuFalse_tousLus_retourneZero() {
        long count = messageRepository
                .countByConversationIdAndDestinataireIdAndLuFalse("conv1", "user1");

        assertThat(count).isEqualTo(1); // msg2 est non lu par user1
    }

    // =========================================================
    // findRecentMessagesAfter (@Query)
    // =========================================================

    @Test
    @DisplayName("findRecentMessagesAfter : retourne les messages après une date")
    void findRecentMessagesAfter_retourneMessagesRecents() {
        Date dateRef = new Date(System.currentTimeMillis() - 4000); // avant msg2 et msg3

        List<Message> result = messageRepository
                .findRecentMessagesAfter("conv1", dateRef);

        assertThat(result).hasSize(2); // msg2 + msg3
        assertThat(result).allMatch(m -> m.getDateCreation().after(dateRef));
    }

    @Test
    @DisplayName("findRecentMessagesAfter : date future → aucun message")
    void findRecentMessagesAfter_dateFuture_retourneVide() {
        Date dateFuture = new Date(System.currentTimeMillis() + 100000);

        List<Message> result = messageRepository
                .findRecentMessagesAfter("conv1", dateFuture);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findRecentMessagesAfter : mauvaise conversation → liste vide")
    void findRecentMessagesAfter_mauvaisConversation_retourneVide() {
        Date dateRef = new Date(0); // epoch - tous les messages après

        List<Message> result = messageRepository
                .findRecentMessagesAfter("conv_inconnue", dateRef);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // deleteByConversationId
    // =========================================================

    @Test
    @DisplayName("deleteByConversationId : supprime tous les messages d'une conversation")
    void deleteByConversationId_supprimeTousLesMessages() {
        messageRepository.deleteByConversationId("conv1");

        List<Message> remaining = messageRepository
                .findByConversationIdOrderByDateCreationAsc("conv1");

        assertThat(remaining).isEmpty();
    }

    @Test
    @DisplayName("deleteByConversationId : ne supprime pas les messages des autres conversations")
    void deleteByConversationId_neSupprimesPasAutresConversations() {
        messageRepository.deleteByConversationId("conv1");

        List<Message> conv2Messages = messageRepository
                .findByConversationIdOrderByDateCreationAsc("conv2");

        assertThat(conv2Messages).hasSize(1);
    }
}
