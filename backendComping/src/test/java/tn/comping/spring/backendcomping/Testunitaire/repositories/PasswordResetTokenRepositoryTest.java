package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import tn.comping.spring.backendcomping.entities.PasswordResetToken;
import tn.comping.spring.backendcomping.repositories.PasswordResetTokenRepository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - PasswordResetTokenRepository")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private PasswordResetToken tokenValide;
    private PasswordResetToken tokenExpire;
    private PasswordResetToken tokenUtilise;

    @BeforeEach
    void setUp() {
        tokenValide = tokenRepository.save(PasswordResetToken.builder()
                .userId("user1").token("token-valide-uuid")
                .expiryDate(Date.from(Instant.now().plusSeconds(3600)))
                .used(false).build());

        tokenExpire = tokenRepository.save(PasswordResetToken.builder()
                .userId("user1").token("token-expire-uuid")
                .expiryDate(Date.from(Instant.now().minusSeconds(3600)))
                .used(false).build());

        tokenUtilise = tokenRepository.save(PasswordResetToken.builder()
                .userId("user1").token("token-utilise-uuid")
                .expiryDate(Date.from(Instant.now().plusSeconds(3600)))
                .used(true).build());

        // Token pour user2
        tokenRepository.save(PasswordResetToken.builder()
                .userId("user2").token("token-user2-uuid")
                .expiryDate(Date.from(Instant.now().plusSeconds(3600)))
                .used(false).build());
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
    }

    // =========================================================
    // findByToken
    // =========================================================

    @Test
    @DisplayName("findByToken : token existant → retourne le token")
    void findByToken_tokenExistant_retourneToken() {
        Optional<PasswordResetToken> result = tokenRepository
                .findByToken("token-valide-uuid");

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user1");
        assertThat(result.get().isUsed()).isFalse();
    }

    @Test
    @DisplayName("findByToken : token inexistant → absent")
    void findByToken_tokenInexistant_absent() {
        Optional<PasswordResetToken> result = tokenRepository
                .findByToken("token-qui-nexiste-pas");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByToken : token expiré toujours retrouvable")
    void findByToken_tokenExpire_retrouvable() {
        Optional<PasswordResetToken> result = tokenRepository
                .findByToken("token-expire-uuid");

        assertThat(result).isPresent();
        assertThat(result.get().getExpiryDate()).isBefore(new Date());
    }

    @Test
    @DisplayName("findByToken : token déjà utilisé retrouvable avec used=true")
    void findByToken_tokenUtilise_retrouvableAvecUsedTrue() {
        Optional<PasswordResetToken> result = tokenRepository
                .findByToken("token-utilise-uuid");

        assertThat(result).isPresent();
        assertThat(result.get().isUsed()).isTrue();
    }

    // =========================================================
    // findByUserId
    // =========================================================

    @Test
    @DisplayName("findByUserId : retourne tous les tokens d'un utilisateur")
    void findByUserId_retourneTousLesTokens() {
        List<PasswordResetToken> result = tokenRepository.findByUserId("user1");

        assertThat(result).hasSize(3); // tokenValide + tokenExpire + tokenUtilise
        assertThat(result).allMatch(t -> "user1".equals(t.getUserId()));
    }

    @Test
    @DisplayName("findByUserId : user2 a un seul token")
    void findByUserId_user2_unSeulToken() {
        List<PasswordResetToken> result = tokenRepository.findByUserId("user2");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByUserId : utilisateur sans token → liste vide")
    void findByUserId_sansToken_retourneVide() {
        List<PasswordResetToken> result = tokenRepository.findByUserId("user_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByUserIdAndUsedFalseAndExpiryDateAfter
    // =========================================================

    @Test
    @DisplayName("findByUserIdAndUsedFalseAndExpiryDateAfter : retourne uniquement les tokens valides et non utilisés")
    void findByUserIdAndUsedFalseAndExpiryDateAfter_retourneTokensActifs() {
        List<PasswordResetToken> result = tokenRepository
                .findByUserIdAndUsedFalseAndExpiryDateAfter("user1", new Date());

        // Seul tokenValide : non utilisé ET non expiré
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToken()).isEqualTo("token-valide-uuid");
    }

    @Test
    @DisplayName("findByUserIdAndUsedFalseAndExpiryDateAfter : exclut les tokens expirés")
    void findByUserIdAndUsedFalseAndExpiryDateAfter_exclutTokensExpires() {
        List<PasswordResetToken> result = tokenRepository
                .findByUserIdAndUsedFalseAndExpiryDateAfter("user1", new Date());

        assertThat(result).noneMatch(t -> t.getToken().equals("token-expire-uuid"));
    }

    @Test
    @DisplayName("findByUserIdAndUsedFalseAndExpiryDateAfter : exclut les tokens utilisés")
    void findByUserIdAndUsedFalseAndExpiryDateAfter_exclutTokensUtilises() {
        List<PasswordResetToken> result = tokenRepository
                .findByUserIdAndUsedFalseAndExpiryDateAfter("user1", new Date());

        assertThat(result).noneMatch(t -> t.getToken().equals("token-utilise-uuid"));
    }

    @Test
    @DisplayName("findByUserIdAndUsedFalseAndExpiryDateAfter : utilisateur sans token actif → liste vide")
    void findByUserIdAndUsedFalseAndExpiryDateAfter_sansTokenActif_retourneVide() {
        List<PasswordResetToken> result = tokenRepository
                .findByUserIdAndUsedFalseAndExpiryDateAfter("user_inconnu", new Date());

        assertThat(result).isEmpty();
    }

    // =========================================================
    // deleteAll et save
    // =========================================================

    @Test
    @DisplayName("deleteAll de la liste : supprime tous les tokens d'un utilisateur")
    void deleteAll_supprimeTousLesTokensUtilisateur() {
        List<PasswordResetToken> tokensUser1 = tokenRepository.findByUserId("user1");
        tokenRepository.deleteAll(tokensUser1);

        assertThat(tokenRepository.findByUserId("user1")).isEmpty();
        // user2 non impacté
        assertThat(tokenRepository.findByUserId("user2")).hasSize(1);
    }

    @Test
    @DisplayName("save : marquer un token comme utilisé et le retrouver mis à jour")
    void save_marquerTokenUtilise_persisteChangement() {
        tokenValide.setUsed(true);
        tokenRepository.save(tokenValide);

        Optional<PasswordResetToken> result = tokenRepository.findByToken("token-valide-uuid");
        assertThat(result).isPresent();
        assertThat(result.get().isUsed()).isTrue();
    }
}
