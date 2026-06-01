package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.repositories.ConversationRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - ConversationRepository")
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    private Conversation conv1to2;
    private Conversation conv1to3;
    private Conversation conv3to4;

    @BeforeEach
    void setUp() {
        conv1to2 = conversationRepository.save(Conversation.builder()
                .participant1Id("user1").participant2Id("user2")
                .groupe(false).messagesNonLusP1(0).messagesNonLusP2(2)
                .dateDernierMessage(new Date(System.currentTimeMillis() - 3000))
                .dateCreation(new Date()).build());

        conv1to3 = conversationRepository.save(Conversation.builder()
                .participant1Id("user1").participant2Id("user3")
                .groupe(false).messagesNonLusP1(1).messagesNonLusP2(0)
                .dateDernierMessage(new Date())
                .dateCreation(new Date()).build());

        conv3to4 = conversationRepository.save(Conversation.builder()
                .participant1Id("user3").participant2Id("user4")
                .groupe(false).messagesNonLusP1(0).messagesNonLusP2(0)
                .dateDernierMessage(new Date(System.currentTimeMillis() - 10000))
                .dateCreation(new Date()).build());
    }

    @AfterEach
    void tearDown() {
        conversationRepository.deleteAll();
    }

    // =========================================================
    // findByParticipant1IdAndParticipant2Id
    // =========================================================

    @Test
    @DisplayName("findByParticipant1IdAndParticipant2Id : conversation existante trouvée")
    void findByParticipant1IdAndParticipant2Id_convExistante_trouvee() {
        Optional<Conversation> result = conversationRepository
                .findByParticipant1IdAndParticipant2Id("user1", "user2");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(conv1to2.getId());
    }

    @Test
    @DisplayName("findByParticipant1IdAndParticipant2Id : participants inversés → absent")
    void findByParticipant1IdAndParticipant2Id_participantsInverses_absent() {
        // user2 n'est pas le participant1 dans conv1to2
        Optional<Conversation> result = conversationRepository
                .findByParticipant1IdAndParticipant2Id("user2", "user1");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByParticipant1IdAndParticipant2Id : conversation inexistante → absent")
    void findByParticipant1IdAndParticipant2Id_convInexistante_absent() {
        Optional<Conversation> result = conversationRepository
                .findByParticipant1IdAndParticipant2Id("user5", "user6");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByParticipant2IdAndParticipant1Id
    // =========================================================

    @Test
    @DisplayName("findByParticipant2IdAndParticipant1Id : recherche avec ordre inversé")
    void findByParticipant2IdAndParticipant1Id_retourneConversation() {
        Optional<Conversation> result = conversationRepository
                .findByParticipant2IdAndParticipant1Id("user2", "user1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(conv1to2.getId());
    }

    // =========================================================
    // findByParticipant1IdOrParticipant2Id
    // =========================================================

    @Test
    @DisplayName("findByParticipant1IdOrParticipant2Id : retourne toutes les conversations d'un utilisateur")
    void findByParticipant1IdOrParticipant2Id_retourneConversationsUtilisateur() {
        List<Conversation> result = conversationRepository
                .findByParticipant1IdOrParticipant2Id("user1", "user1");

        assertThat(result).hasSize(2); // conv1to2 + conv1to3
        assertThat(result).allMatch(c ->
                "user1".equals(c.getParticipant1Id()) || "user1".equals(c.getParticipant2Id()));
    }

    @Test
    @DisplayName("findByParticipant1IdOrParticipant2Id : user3 apparaît comme p1 et p2")
    void findByParticipant1IdOrParticipant2Id_user3_convDesDeuxCotes() {
        List<Conversation> result = conversationRepository
                .findByParticipant1IdOrParticipant2Id("user3", "user3");

        assertThat(result).hasSize(2); // conv1to3 (user3 = p2) + conv3to4 (user3 = p1)
    }

    @Test
    @DisplayName("findByParticipant1IdOrParticipant2Id : utilisateur sans conversation → liste vide")
    void findByParticipant1IdOrParticipant2Id_sansConversation_retourneVide() {
        List<Conversation> result = conversationRepository
                .findByParticipant1IdOrParticipant2Id("user_inconnu", "user_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc
    // =========================================================

    @Test
    @DisplayName("findByP1OrP2OrderByDateDesc : conversations ordonnées par date décroissante")
    void findByP1OrP2OrderByDateDernierMessageDesc_retourneConvOrdonnees() {
        List<Conversation> result = conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc("user1", "user1");

        assertThat(result).hasSize(2);
        // conv1to3 est la plus récente (dateDernierMessage = now)
        assertThat(result.get(0).getId()).isEqualTo(conv1to3.getId());
        assertThat(result.get(1).getId()).isEqualTo(conv1to2.getId());
    }

    @Test
    @DisplayName("findByP1OrP2OrderByDateDesc : user sans conversation → liste vide")
    void findByP1OrP2OrderByDateDernierMessageDesc_sansConv_retourneVide() {
        List<Conversation> result = conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc("user_new", "user_new");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // CRUD de base
    // =========================================================

    @Test
    @DisplayName("save et findById : conversation sauvegardée récupérable")
    void save_puisfindById_retourneConversation() {
        Conversation conv = conversationRepository.save(Conversation.builder()
                .participant1Id("user5").participant2Id("user6")
                .groupe(false).dateCreation(new Date()).dateDernierMessage(new Date()).build());

        assertThat(conversationRepository.findById(conv.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById : conversation supprimée non retrouvable")
    void deleteById_convSupprimee() {
        conversationRepository.deleteById(conv1to2.getId());
        assertThat(conversationRepository.findById(conv1to2.getId())).isEmpty();
    }
}
