package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import tn.comping.spring.backendcomping.repositories.AvisRepository;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - AvisRepository")
class AvisRepositoryTest {

    @Autowired
    private AvisRepository avisRepository;

    private Avis avisValide1;
    private Avis avisValide2;
    private Avis avisNonValide;
    private Avis avisReponse;

    @BeforeEach
    void setUp() {
        avisValide1 = avisRepository.save(Avis.builder()
                .note(5).commentaire("Excellent camping!").utilisateurId("user1")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.VALIDE).valide(true)
                .datePublication(new Date()).build());

        avisValide2 = avisRepository.save(Avis.builder()
                .note(4).commentaire("Très bien").utilisateurId("user2")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.VALIDE).valide(true)
                .datePublication(new Date()).build());

        avisNonValide = avisRepository.save(Avis.builder()
                .note(3).commentaire("Bien").utilisateurId("user1")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.EN_ATTENTE).valide(false)
                .datePublication(new Date()).build());

        avisReponse = avisRepository.save(Avis.builder()
                .note(5).commentaire("Merci pour votre avis").utilisateurId("user3")
                .cibleId("site1").typeCible(TypeCible.SITE_CAMPING)
                .statut(StatutAvis.VALIDE).valide(true)
                .parentAvisId(avisValide1.getId())
                .datePublication(new Date()).build());
    }

    @AfterEach
    void tearDown() {
        avisRepository.deleteAll();
    }

    // =========================================================
    // findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByCibleIdAndTypeCible_Valide_ParentNull : retourne avis racines valides")
    void findByCibleIdAndTypeCibleAndValide_sansPArent_retourneAvisRacines() {
        List<Avis> result = avisRepository
                .findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                        "site1", TypeCible.SITE_CAMPING, true);

        assertThat(result).hasSize(2);
        assertThat(result).noneMatch(a -> a.getParentAvisId() != null);
        assertThat(result).allMatch(Avis::isValide);
    }

    @Test
    @DisplayName("findByCibleIdAndTypeCible_Valide_ParentNull : exclut les avis non validés")
    void findByCibleIdAndTypeCibleAndValide_exclutNonValides() {
        List<Avis> result = avisRepository
                .findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                        "site1", TypeCible.SITE_CAMPING, true);

        assertThat(result).noneMatch(a -> a.getId().equals(avisNonValide.getId()));
    }

    @Test
    @DisplayName("findByCibleIdAndTypeCible_Valide_ParentNull : autre cibleId → liste vide")
    void findByCibleIdAndTypeCibleAndValide_autreCible_retourneVide() {
        List<Avis> result = avisRepository
                .findByCibleIdAndTypeCibleAndValideAndParentAvisIdIsNullOrderByDatePublicationDesc(
                        "site_inconnu", TypeCible.SITE_CAMPING, true);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByParentAvisIdOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByParentAvisId : retourne les réponses à un avis")
    void findByParentAvisId_retourneEnfants() {
        List<Avis> result = avisRepository
                .findByParentAvisIdOrderByDatePublicationDesc(avisValide1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(avisReponse.getId());
    }

    @Test
    @DisplayName("findByParentAvisId : avis sans enfants → liste vide")
    void findByParentAvisId_sansEnfants_retourneVide() {
        List<Avis> result = avisRepository
                .findByParentAvisIdOrderByDatePublicationDesc(avisValide2.getId());

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByUtilisateurIdOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByUtilisateurId : retourne tous les avis d'un utilisateur")
    void findByUtilisateurId_retourneAvisUtilisateur() {
        List<Avis> result = avisRepository
                .findByUtilisateurIdOrderByDatePublicationDesc("user1");

        assertThat(result).hasSize(2); // avisValide1 + avisNonValide
        assertThat(result).allMatch(a -> "user1".equals(a.getUtilisateurId()));
    }

    @Test
    @DisplayName("findByUtilisateurId : utilisateur sans avis → liste vide")
    void findByUtilisateurId_utilisateurSansAvis_retourneVide() {
        List<Avis> result = avisRepository
                .findByUtilisateurIdOrderByDatePublicationDesc("user_inexistant");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByStatutOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByStatut : VALIDE → retourne les avis validés")
    void findByStatut_valide_retourneAvisValides() {
        List<Avis> result = avisRepository
                .findByStatutOrderByDatePublicationDesc(StatutAvis.VALIDE);

        assertThat(result).hasSize(3); // avisValide1 + avisValide2 + avisReponse
        assertThat(result).allMatch(a -> a.getStatut() == StatutAvis.VALIDE);
    }

    @Test
    @DisplayName("findByStatut : EN_ATTENTE → retourne les avis en attente")
    void findByStatut_enAttente_retourneAvisEnAttente() {
        List<Avis> result = avisRepository
                .findByStatutOrderByDatePublicationDesc(StatutAvis.EN_ATTENTE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(avisNonValide.getId());
    }

    @Test
    @DisplayName("findByStatut : REJETE → liste vide (aucun rejeté)")
    void findByStatut_rejete_retourneVide() {
        List<Avis> result = avisRepository
                .findByStatutOrderByDatePublicationDesc(StatutAvis.REJETE);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByStatutAndParentAvisIdIsNullOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByStatutAndParentNull : VALIDE → seulement les avis racines validés")
    void findByStatutAndParentNull_valide_retourneRacinesValides() {
        List<Avis> result = avisRepository
                .findByStatutAndParentAvisIdIsNullOrderByDatePublicationDesc(StatutAvis.VALIDE);

        assertThat(result).hasSize(2); // avisValide1 + avisValide2 (sans avisReponse qui a un parent)
        assertThat(result).noneMatch(a -> a.getParentAvisId() != null);
    }

    // =========================================================
    // countByCibleIdAndTypeCibleAndValide
    // =========================================================

    @Test
    @DisplayName("countByCibleIdAndTypeCibleAndValide : compte les avis valides pour une cible")
    void countByCibleIdAndTypeCibleAndValide_retourneNombre() {
        long count = avisRepository
                .countByCibleIdAndTypeCibleAndValide("site1", TypeCible.SITE_CAMPING, true);

        assertThat(count).isEqualTo(3); // avisValide1 + avisValide2 + avisReponse
    }

    @Test
    @DisplayName("countByCibleIdAndTypeCibleAndValide : cible inconnue → 0")
    void countByCibleIdAndTypeCibleAndValide_cibleInconnue_retourneZero() {
        long count = avisRepository
                .countByCibleIdAndTypeCibleAndValide("cible_inconnue", TypeCible.PRODUIT, true);

        assertThat(count).isEqualTo(0);
    }

    // =========================================================
    // findByCibleIdAndTypeCible
    // =========================================================

    @Test
    @DisplayName("findByCibleIdAndTypeCible : retourne tous les avis (valides et non valides)")
    void findByCibleIdAndTypeCible_retourneTousLesAvis() {
        List<Avis> result = avisRepository
                .findByCibleIdAndTypeCible("site1", TypeCible.SITE_CAMPING);

        assertThat(result).hasSize(4); // tous les avis pour site1
    }

    // =========================================================
    // findByUtilisateurIdInAndValideOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByUtilisateurIdIn_valide : retourne les avis valides des utilisateurs listés")
    void findByUtilisateurIdInAndValide_retourneAvisValides() {
        List<Avis> result = avisRepository
                .findByUtilisateurIdInAndValideOrderByDatePublicationDesc(
                        List.of("user1", "user2"), true);

        assertThat(result).hasSize(2); // avisValide1 (user1) + avisValide2 (user2)
        assertThat(result).allMatch(Avis::isValide);
    }

    @Test
    @DisplayName("findByUtilisateurIdIn_valide : liste vide d'IDs → liste vide")
    void findByUtilisateurIdInAndValide_listeVide_retourneVide() {
        List<Avis> result = avisRepository
                .findByUtilisateurIdInAndValideOrderByDatePublicationDesc(List.of(), true);

        assertThat(result).isEmpty();
    }
}
