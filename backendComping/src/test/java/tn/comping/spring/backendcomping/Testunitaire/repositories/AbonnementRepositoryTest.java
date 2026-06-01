package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - AbonnementRepository")
class AbonnementRepositoryTest {

    @Autowired
    private AbonnementRepository abonnementRepository;

    @BeforeEach
    void setUp() {
        // user1 suit user2, user3
        abonnementRepository.save(Abonnement.builder().suiveurId("user1").suiviId("user2").build());
        abonnementRepository.save(Abonnement.builder().suiveurId("user1").suiviId("user3").build());
        // user2 suit user1
        abonnementRepository.save(Abonnement.builder().suiveurId("user2").suiviId("user1").build());
        // user3 suit user2
        abonnementRepository.save(Abonnement.builder().suiveurId("user3").suiviId("user2").build());
    }

    @AfterEach
    void tearDown() {
        abonnementRepository.deleteAll();
    }

    // =========================================================
    // findBySuiveurId
    // =========================================================

    @Test
    @DisplayName("findBySuiveurId : retourne tous les abonnements d'un suiveur")
    void findBySuiveurId_retourneAbonnements() {
        List<Abonnement> result = abonnementRepository.findBySuiveurId("user1");

        assertThat(result).hasSize(2); // user1 suit user2 et user3
        assertThat(result).allMatch(a -> "user1".equals(a.getSuiveurId()));
        assertThat(result).extracting(Abonnement::getSuiviId)
                .containsExactlyInAnyOrder("user2", "user3");
    }

    @Test
    @DisplayName("findBySuiveurId : utilisateur sans abonnement → liste vide")
    void findBySuiveurId_sansAbonnement_retourneVide() {
        List<Abonnement> result = abonnementRepository.findBySuiveurId("user_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findBySuiviId
    // =========================================================

    @Test
    @DisplayName("findBySuiviId : retourne tous les abonnés d'un utilisateur")
    void findBySuiviId_retourneAbonnes() {
        List<Abonnement> result = abonnementRepository.findBySuiviId("user2");

        assertThat(result).hasSize(2); // user1 et user3 suivent user2
        assertThat(result).allMatch(a -> "user2".equals(a.getSuiviId()));
        assertThat(result).extracting(Abonnement::getSuiveurId)
                .containsExactlyInAnyOrder("user1", "user3");
    }

    @Test
    @DisplayName("findBySuiviId : aucun abonné → liste vide")
    void findBySuiviId_sansAbonne_retourneVide() {
        List<Abonnement> result = abonnementRepository.findBySuiviId("user_inconnu");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findBySuiviId : user3 n'a qu'un seul abonné (user1)")
    void findBySuiviId_user3_unSeulAbonne() {
        List<Abonnement> result = abonnementRepository.findBySuiviId("user3");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSuiveurId()).isEqualTo("user1");
    }

    // =========================================================
    // countBySuiveurId
    // =========================================================

    @Test
    @DisplayName("countBySuiveurId : compte le nombre d'abonnements (following)")
    void countBySuiveurId_retourneNombreAbonnements() {
        long count = abonnementRepository.countBySuiveurId("user1");

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countBySuiveurId : user2 suit 1 personne")
    void countBySuiveurId_user2_suivUnePersonne() {
        long count = abonnementRepository.countBySuiveurId("user2");

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countBySuiveurId : utilisateur sans abonnement → 0")
    void countBySuiveurId_sansAbonnement_retourneZero() {
        long count = abonnementRepository.countBySuiveurId("user_inconnu");

        assertThat(count).isEqualTo(0);
    }

    // =========================================================
    // countBySuiviId
    // =========================================================

    @Test
    @DisplayName("countBySuiviId : compte le nombre d'abonnés (followers)")
    void countBySuiviId_retourneNombreAbonnes() {
        long count = abonnementRepository.countBySuiviId("user2");

        assertThat(count).isEqualTo(2); // user1 et user3 suivent user2
    }

    @Test
    @DisplayName("countBySuiviId : user1 a 1 abonné")
    void countBySuiviId_user1_unAbonne() {
        long count = abonnementRepository.countBySuiviId("user1");

        assertThat(count).isEqualTo(1); // seul user2 suit user1
    }

    // =========================================================
    // existsBySuiveurIdAndSuiviId
    // =========================================================

    @Test
    @DisplayName("existsBySuiveurIdAndSuiviId : abonnement existant → true")
    void existsBySuiveurIdAndSuiviId_existant_retourneTrue() {
        boolean exists = abonnementRepository.existsBySuiveurIdAndSuiviId("user1", "user2");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsBySuiveurIdAndSuiviId : abonnement inexistant → false")
    void existsBySuiveurIdAndSuiviId_inexistant_retourneFalse() {
        boolean exists = abonnementRepository.existsBySuiveurIdAndSuiviId("user2", "user3");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsBySuiveurIdAndSuiviId : ordre inversé → false (user2 ne suit pas user3)")
    void existsBySuiveurIdAndSuiviId_ordreInverse_retourneFalse() {
        boolean exists = abonnementRepository.existsBySuiveurIdAndSuiviId("user3", "user1");

        assertThat(exists).isFalse();
    }

    // =========================================================
    // deleteBySuiveurIdAndSuiviId
    // =========================================================

    @Test
    @DisplayName("deleteBySuiveurIdAndSuiviId : supprime l'abonnement spécifique")
    void deleteBySuiveurIdAndSuiviId_supprimeAbonnement() {
        abonnementRepository.deleteBySuiveurIdAndSuiviId("user1", "user2");

        assertThat(abonnementRepository.existsBySuiveurIdAndSuiviId("user1", "user2")).isFalse();
        // L'autre abonnement de user1 est intact
        assertThat(abonnementRepository.existsBySuiveurIdAndSuiviId("user1", "user3")).isTrue();
    }

    @Test
    @DisplayName("deleteBySuiveurIdAndSuiviId : ne supprime pas les autres abonnements")
    void deleteBySuiveurIdAndSuiviId_preserveAutresAbonnements() {
        long countBefore = abonnementRepository.count();

        abonnementRepository.deleteBySuiveurIdAndSuiviId("user1", "user2");

        assertThat(abonnementRepository.count()).isEqualTo(countBefore - 1);
    }
}
