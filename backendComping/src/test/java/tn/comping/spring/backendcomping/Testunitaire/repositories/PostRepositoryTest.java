package tn.comping.spring.backendcomping.Testunitaire.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.PostRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DisplayName("Tests repository - PostRepository")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    private Post postUser1A;
    private Post postUser1B;
    private Post postUser2;
    private Post postAvisLie;

    @BeforeEach
    void setUp() {
        postUser1A = postRepository.save(Post.builder()
                .auteurId("user1").contenu("Post A de user1 #camping")
                .typePost("FEED").visibilite("PUBLIC")
                .datePublication(new Date(System.currentTimeMillis() - 2000))
                .likesCount(5).commentairesCount(2)
                .reactions(new HashMap<>()).hashtags(List.of("camping"))
                .images(List.of()).trendScore(50.0).build());

        postUser1B = postRepository.save(Post.builder()
                .auteurId("user1").contenu("Post B de user1 #nature")
                .typePost("FEED").visibilite("PUBLIC")
                .datePublication(new Date())
                .likesCount(10).commentairesCount(5)
                .reactions(new HashMap<>()).hashtags(List.of("nature"))
                .images(List.of()).trendScore(80.0).build());

        postUser2 = postRepository.save(Post.builder()
                .auteurId("user2").contenu("Post de user2")
                .typePost("FEED").visibilite("AMIS")
                .datePublication(new Date(System.currentTimeMillis() - 5000))
                .likesCount(0).commentairesCount(0)
                .reactions(new HashMap<>()).hashtags(List.of())
                .images(List.of()).trendScore(0.0).build());

        postAvisLie = postRepository.save(Post.builder()
                .auteurId("user1").contenu("Post lié à un avis")
                .typePost("AVIS_LIE").avisId("avis1")
                .cibleType("AVIS").cibleId("avis1")
                .visibilite("PUBLIC")
                .datePublication(new Date(System.currentTimeMillis() - 10000))
                .likesCount(0).commentairesCount(0)
                .reactions(new HashMap<>()).hashtags(List.of())
                .images(List.of()).trendScore(0.0).build());
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
    }

    // =========================================================
    // findByAuteurIdOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByAuteurId : retourne les posts d'un auteur ordonnés par date décroissante")
    void findByAuteurId_retournePostsOrdonnésParDate() {
        List<Post> result = postRepository
                .findByAuteurIdOrderByDatePublicationDesc("user1");

        assertThat(result).hasSize(3); // postUser1A + postUser1B + postAvisLie
        assertThat(result).allMatch(p -> "user1".equals(p.getAuteurId()));
        // Le plus récent en premier
        assertThat(result.get(0).getId()).isEqualTo(postUser1B.getId());
    }

    @Test
    @DisplayName("findByAuteurId avec pageable : limite le nombre de résultats")
    void findByAuteurId_avecPageable_limiteResultats() {
        PageRequest pageable = PageRequest.of(0, 2, Sort.by("datePublication").descending());
        List<Post> result = postRepository
                .findByAuteurIdOrderByDatePublicationDesc("user1", pageable);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(postUser1B.getId());
    }

    @Test
    @DisplayName("findByAuteurId : auteur sans posts → liste vide")
    void findByAuteurId_auteurSansPosts_retourneVide() {
        List<Post> result = postRepository
                .findByAuteurIdOrderByDatePublicationDesc("user_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByAvisId
    // =========================================================

    @Test
    @DisplayName("findByAvisId : retourne les posts liés à un avis")
    void findByAvisId_retournePostsLies() {
        List<Post> result = postRepository.findByAvisId("avis1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(postAvisLie.getId());
    }

    @Test
    @DisplayName("findByAvisId : avis sans post lié → liste vide")
    void findByAvisId_sansPostLie_retourneVide() {
        List<Post> result = postRepository.findByAvisId("avis_inconnu");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByCibleIdAndCibleTypeOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByCibleIdAndCibleType : retourne les posts pour une cible")
    void findByCibleIdAndCibleType_retournePostsCible() {
        List<Post> result = postRepository
                .findByCibleIdAndCibleTypeOrderByDatePublicationDesc("avis1", "AVIS");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCibleType()).isEqualTo("AVIS");
        assertThat(result.get(0).getCibleId()).isEqualTo("avis1");
    }

    @Test
    @DisplayName("findByCibleIdAndCibleType : cible inexistante → liste vide")
    void findByCibleIdAndCibleType_cibleInexistante_retourneVide() {
        List<Post> result = postRepository
                .findByCibleIdAndCibleTypeOrderByDatePublicationDesc("cible_inconnu", "POST");

        assertThat(result).isEmpty();
    }

    // =========================================================
    // findByAuteurIdInOrderByDatePublicationDesc
    // =========================================================

    @Test
    @DisplayName("findByAuteurIdIn : retourne les posts de plusieurs auteurs avec pagination")
    void findByAuteurIdIn_retournePostsMultiplesAuteurs() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("datePublication").descending());
        List<Post> result = postRepository
                .findByAuteurIdInOrderByDatePublicationDesc(List.of("user1", "user2"), pageable);

        assertThat(result).hasSize(4); // tous les posts de user1 et user2
        assertThat(result).allMatch(p ->
                "user1".equals(p.getAuteurId()) || "user2".equals(p.getAuteurId()));
    }

    @Test
    @DisplayName("findByAuteurIdIn : liste d'auteurs avec pagination limitée")
    void findByAuteurIdIn_avecPagination_limiteResultats() {
        PageRequest pageable = PageRequest.of(0, 2, Sort.by("datePublication").descending());
        List<Post> result = postRepository
                .findByAuteurIdInOrderByDatePublicationDesc(List.of("user1", "user2"), pageable);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByAuteurIdIn : liste vide d'auteurs → liste vide")
    void findByAuteurIdIn_listeVide_retourneVide() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Post> result = postRepository
                .findByAuteurIdInOrderByDatePublicationDesc(List.of(), pageable);

        assertThat(result).isEmpty();
    }

    // =========================================================
    // Opérations CRUD de base
    // =========================================================

    @Test
    @DisplayName("save et findById : post sauvegardé récupérable par son ID")
    void save_puisfindById_retournePost() {
        Post post = postRepository.save(Post.builder()
                .auteurId("user3").contenu("Test save")
                .typePost("FEED").visibilite("PUBLIC")
                .datePublication(new Date())
                .reactions(new HashMap<>()).hashtags(List.of()).images(List.of()).build());

        assertThat(postRepository.findById(post.getId())).isPresent();
    }

    @Test
    @DisplayName("deleteById : post supprimé non retrouvable")
    void deleteById_postSupprime() {
        postRepository.deleteById(postUser1A.getId());
        assertThat(postRepository.findById(postUser1A.getId())).isEmpty();
    }
}
