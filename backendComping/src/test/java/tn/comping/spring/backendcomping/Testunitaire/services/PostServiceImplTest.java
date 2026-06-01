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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import tn.comping.spring.backendcomping.Testunitaire.repositories.*;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.AbonnementRepository;
import tn.comping.spring.backendcomping.repositories.CommentaireRepository;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.PostServiceImpl;
import tn.comping.spring.backendcomping.utils.mapper.PostMapper;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires - PostServiceImpl")
class PostServiceImplTest {

    @Mock private PostRepository postRepository;
    @Mock private InteractionRepository interactionRepository;
    @Mock private SignupRepository signupRepository;
    @Mock private PostMapper postMapper;
    @Mock private CommentaireRepository commentaireRepository;
    @Mock private AbonnementRepository abonnementRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private PostServiceImpl postService;

    private SignupEntity auteur;
    private Post post;
    private PostRequestDTO validDto;
    private PostResponseDTO postResponse;

    @BeforeEach
    void setUp() {
        auteur = SignupEntity.builder()
                .id("user1")
                .email("auteur@test.com")
                .firstName("Jean")
                .lastName("Dupont")
                .build();

        post = Post.builder()
                .id("post1")
                .auteurId("auteur@test.com")
                .contenu("Mon super post #camping")
                .typePost("FEED")
                .datePublication(new Date())
                .likesCount(0)
                .commentairesCount(0)
                .reactions(new HashMap<>())
                .hashtags(List.of("camping"))
                .visibilite("PUBLIC")
                .images(List.of())
                .build();

        validDto = PostRequestDTO.builder()
                .contenu("Mon super post #camping")
                .visibilite("PUBLIC")
                .build();

        postResponse = PostResponseDTO.builder()
                .id("post1")
                .auteurId("auteur@test.com")
                .auteurNom("Jean Dupont")
                .contenu("Mon super post #camping")
                .hashtags(List.of("camping"))
                .visibilite("PUBLIC")
                .build();
    }

    // =========================================================
    // createPost
    // =========================================================

    @Test
    @DisplayName("createPost : contenu valide → post créé avec hashtags extraits")
    void createPost_contenuValide_retournePostAvecHashtags() {
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(abonnementRepository.findBySuiviId("auteur@test.com")).thenReturn(List.of());
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        PostResponseDTO result = postService.createPost(validDto, "auteur@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getContenu()).isEqualTo("Mon super post #camping");
        verify(postRepository).save(argThat(p ->
                p.getHashtags().contains("camping") && "auteur@test.com".equals(p.getAuteurId())));
    }

    @Test
    @DisplayName("createPost : contenu vide → exception 400")
    void createPost_contenuVide_lanceException400() {
        PostRequestDTO dtoInvalide = PostRequestDTO.builder().contenu("   ").build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));

        assertThatThrownBy(() -> postService.createPost(dtoInvalide, "auteur@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPost : utilisateur inexistant → exception 404")
    void createPost_utilisateurInexistant_lanceException404() {
        when(signupRepository.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());
        when(signupRepository.findById("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(validDto, "inconnu@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("createPost : visibilité null → par défaut PUBLIC")
    void createPost_visibiliteNull_appliquePUBLIC() {
        PostRequestDTO dtoSansVisibilite = PostRequestDTO.builder()
                .contenu("Post sans visibilité").visibilite(null).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(abonnementRepository.findBySuiviId("auteur@test.com")).thenReturn(List.of());
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        postService.createPost(dtoSansVisibilite, "auteur@test.com");

        verify(postRepository).save(argThat(p -> "PUBLIC".equals(p.getVisibilite())));
    }

    @Test
    @DisplayName("createPost : notifie les abonnés après publication")
    void createPost_avecAbonnes_notifieAbonnes() {
        Abonnement abonnement = Abonnement.builder()
                .suiveurId("abonne@test.com").suiviId("auteur@test.com").build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(abonnementRepository.findBySuiviId("auteur@test.com")).thenReturn(List.of(abonnement));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        postService.createPost(validDto, "auteur@test.com");

        verify(messagingTemplate).convertAndSend(
                eq("/topic/user/abonne@test.com/notifications"), any(Map.class));
    }

    // =========================================================
    // updatePost
    // =========================================================

    @Test
    @DisplayName("updatePost : auteur modifie son post → post mis à jour")
    void updatePost_parAuteur_retournePostMisAJour() {
        PostRequestDTO updateDto = PostRequestDTO.builder()
                .contenu("Contenu modifié #nature").visibilite("AMIS").build();

        Post postMaj = Post.builder().id("post1").auteurId("auteur@test.com")
                .contenu("Contenu modifié #nature").hashtags(List.of("nature"))
                .visibilite("AMIS").datePublication(new Date())
                .reactions(new HashMap<>()).images(List.of()).build();

        PostResponseDTO responseMaj = PostResponseDTO.builder()
                .id("post1").contenu("Contenu modifié #nature").visibilite("AMIS").build();

        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(postMaj);
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(responseMaj);

        PostResponseDTO result = postService.updatePost("post1", updateDto, "auteur@test.com");

        assertThat(result).isNotNull();
        verify(postRepository).save(argThat(p -> p.getHashtags().contains("nature")));
    }

    @Test
    @DisplayName("updatePost : autre utilisateur → exception 403")
    void updatePost_parAutreUtilisateur_lanceException403() {
        PostRequestDTO updateDto = PostRequestDTO.builder().contenu("Contenu modifié").build();

        when(postRepository.findById("post1")).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost("post1", updateDto, "autre@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePost : post inexistant → exception 404")
    void updatePost_postInexistant_lanceException404() {
        PostRequestDTO updateDto = PostRequestDTO.builder().contenu("Contenu").build();
        when(postRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost("inexistant", updateDto, "auteur@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // deletePost
    // =========================================================

    @Test
    @DisplayName("deletePost : auteur supprime → commentaires et interactions supprimés")
    void deletePost_parAuteur_supprimeTout() {
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));

        postService.deletePost("post1", "auteur@test.com");

        verify(commentaireRepository).deleteByPostId("post1");
        verify(interactionRepository).deleteByCibleTypeAndCibleId("POST", "post1");
        verify(postRepository).deleteById("post1");
    }

    @Test
    @DisplayName("deletePost : autre utilisateur → exception 403")
    void deletePost_parAutreUtilisateur_lanceException403() {
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost("post1", "autre@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(postRepository, never()).deleteById(any());
    }

    // =========================================================
    // reactToPost / removeReaction
    // =========================================================

    @Test
    @DisplayName("reactToPost : nouvelle réaction 👍 → likesCount incrémenté")
    void reactToPost_nouvelleReactionLike_incrementeLikesCount() {
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                "auteur@test.com", "POST", "post1", "REACTION")).thenReturn(Optional.empty());
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.reactToPost("post1", "auteur@test.com", "👍");

        verify(postRepository).save(argThat(p -> p.getLikesCount() == 1 && p.getReactions().containsKey("👍")));
    }

    @Test
    @DisplayName("reactToPost : changement de réaction → ancienne supprimée, nouvelle ajoutée")
    void reactToPost_changementReaction_remplaceLAncienne() {
        Interaction ancienneReaction = Interaction.builder()
                .id("inter1").auteurId("auteur@test.com")
                .cibleType("POST").cibleId("post1")
                .type("REACTION").emoji("👍").build();

        post.getReactions().put("👍", 1);
        post.setLikesCount(1);

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                "auteur@test.com", "POST", "post1", "REACTION")).thenReturn(Optional.of(ancienneReaction));
        when(interactionRepository.save(any(Interaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.reactToPost("post1", "auteur@test.com", "❤️");

        verify(interactionRepository).delete(ancienneReaction);
        verify(postRepository).save(argThat(p ->
                p.getReactions().containsKey("❤️") && !p.getReactions().containsKey("👍")));
    }

    @Test
    @DisplayName("removeReaction : réaction existante → supprimée du post")
    void removeReaction_reactionExistante_supprimeReaction() {
        Interaction reaction = Interaction.builder()
                .id("inter1").auteurId("auteur@test.com")
                .cibleType("POST").cibleId("post1")
                .type("REACTION").emoji("👍").build();

        post.getReactions().put("👍", 2);
        post.setLikesCount(2);

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                "auteur@test.com", "POST", "post1", "REACTION")).thenReturn(Optional.of(reaction));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.removeReaction("post1", "auteur@test.com");

        verify(interactionRepository).delete(reaction);
        verify(postRepository).save(argThat(p -> p.getLikesCount() == 1));
    }

    @Test
    @DisplayName("removeReaction : aucune réaction existante → aucune action")
    void removeReaction_sansReactionExistante_neRienFait() {
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(interactionRepository.findByAuteurIdAndCibleTypeAndCibleIdAndType(
                "auteur@test.com", "POST", "post1", "REACTION")).thenReturn(Optional.empty());

        postService.removeReaction("post1", "auteur@test.com");

        verify(interactionRepository, never()).delete(any(Interaction.class));
        verify(postRepository, never()).save(any());
    }

    // =========================================================
    // getFeedPosts - visibilité
    // =========================================================

    @Test
    @DisplayName("getFeedPosts : post PUBLIC visible par tous")
    void getFeedPosts_postPublic_visibleParTous() {
        Post postPublic = Post.builder().id("p1").auteurId("autre@test.com")
                .contenu("Post public").visibilite("PUBLIC").datePublication(new Date())
                .reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(abonnementRepository.findBySuiveurId("auteur@test.com")).thenReturn(List.of());
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(postPublic)));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getFeedPosts(0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getFeedPosts : post PRIVE non visible pour autre utilisateur")
    void getFeedPosts_postPrive_nonVisibleParAutres() {
        Post postPrive = Post.builder().id("p1").auteurId("autre@test.com")
                .contenu("Post privé").visibilite("PRIVE").datePublication(new Date())
                .reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(abonnementRepository.findBySuiveurId("auteur@test.com")).thenReturn(List.of());
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(postPrive)));

        List<PostResponseDTO> result = postService.getFeedPosts(0, 10, "auteur@test.com");

        assertThat(result).isEmpty();
        verify(postMapper, never()).toResponseDTO(any(), any());
    }

    @Test
    @DisplayName("getFeedPosts : post AMIS visible uniquement si l'utilisateur suit l'auteur")
    void getFeedPosts_postAmis_visibleSiAbonne() {
        Post postAmis = Post.builder().id("p1").auteurId("ami@test.com")
                .contenu("Post amis").visibilite("AMIS").datePublication(new Date())
                .reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        Abonnement abonnement = Abonnement.builder()
                .suiveurId("auteur@test.com").suiviId("ami@test.com").build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(abonnementRepository.findBySuiveurId("auteur@test.com")).thenReturn(List.of(abonnement));
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(postAmis)));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getFeedPosts(0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    // =========================================================
    // getPostById
    // =========================================================

    @Test
    @DisplayName("getPostById : ID existant → post retourné")
    void getPostById_idExistant_retournePost() {
        when(postRepository.findById("post1")).thenReturn(Optional.of(post));
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        PostResponseDTO result = postService.getPostById("post1", "auteur@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("post1");
    }

    @Test
    @DisplayName("getPostById : ID inexistant → exception 404")
    void getPostById_idInexistant_lanceException404() {
        when(postRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById("inexistant", "auteur@test.com"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // =========================================================
    // getPostsByHashtag
    // =========================================================

    @Test
    @DisplayName("getPostsByHashtag : hashtag avec # → trouve le post correspondant")
    void getPostsByHashtag_avecPrefixDiese_trouvePost() {
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getPostsByHashtag("#camping", 0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getPostsByHashtag : hashtag sans # → même résultat")
    void getPostsByHashtag_sansPrefixDiese_trouvePost() {
        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getPostsByHashtag("camping", 0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    // =========================================================
    // getTrendingPosts
    // =========================================================

    @Test
    @DisplayName("getTrendingPosts : seuls les posts PUBLIC avec trendScore > 0 retournés")
    void getTrendingPosts_filtreTrendingEtPublic() {
        Post trending = Post.builder().id("t1").auteurId("auteur@test.com")
                .contenu("Trending post").visibilite("PUBLIC").trendScore(50.0)
                .datePublication(new Date()).reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        Post nonTrending = Post.builder().id("t2").auteurId("auteur@test.com")
                .contenu("Non trending").visibilite("PUBLIC").trendScore(0.0)
                .datePublication(new Date()).reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(trending, nonTrending)));
        when(postMapper.toResponseDTO(eq(trending), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getTrendingPosts(0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    // =========================================================
    // getFriendsPosts
    // =========================================================

    @Test
    @DisplayName("getFriendsPosts : inclut ses propres posts quel que soit la visibilité")
    void getFriendsPosts_inclutSesPropresPosts() {
        Post postPrive = Post.builder().id("mine").auteurId("auteur@test.com")
                .contenu("Mon post privé").visibilite("PRIVE")
                .datePublication(new Date()).reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findByAuteurIdInOrderByDatePublicationDesc(anyList(), any(Pageable.class)))
                .thenReturn(List.of(postPrive));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getFriendsPosts(List.of(), 0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getFriendsPosts : post AMIS d'un ami visible")
    void getFriendsPosts_postAmisDAmi_visible() {
        Post postAmi = Post.builder().id("ami_post").auteurId("ami@test.com")
                .contenu("Post ami").visibilite("AMIS")
                .datePublication(new Date()).reactions(new HashMap<>()).images(List.of()).hashtags(List.of()).build();

        when(signupRepository.findByEmail("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(signupRepository.findById("auteur@test.com")).thenReturn(Optional.of(auteur));
        when(postRepository.findByAuteurIdInOrderByDatePublicationDesc(anyList(), any(Pageable.class)))
                .thenReturn(List.of(postAmi));
        when(postMapper.toResponseDTO(any(Post.class), anyString())).thenReturn(postResponse);

        List<PostResponseDTO> result = postService.getFriendsPosts(List.of("ami@test.com"), 0, 10, "auteur@test.com");

        assertThat(result).hasSize(1);
    }

    // =========================================================
    // recalculateTrendScores
    // =========================================================

    @Test
    @DisplayName("recalculateTrendScores : met à jour les scores de tous les posts")
    void recalculateTrendScores_metAJourTousLesPosts() {
        Post post1 = Post.builder().id("p1").auteurId("auteur@test.com").contenu("A")
                .datePublication(new Date()).likesCount(10).commentairesCount(5)
                .reactions(new HashMap<>()).hashtags(List.of("a", "b")).images(List.of())
                .visibilite("PUBLIC").build();

        Post post2 = Post.builder().id("p2").auteurId("auteur@test.com").contenu("B")
                .datePublication(new Date(System.currentTimeMillis() - 24 * 3600 * 1000L))
                .likesCount(0).commentairesCount(0)
                .reactions(new HashMap<>()).hashtags(List.of()).images(List.of())
                .visibilite("PUBLIC").build();

        when(postRepository.findAll()).thenReturn(List.of(post1, post2));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        postService.recalculateTrendScores();

        verify(postRepository, times(2)).save(any(Post.class));
        verify(postRepository).save(argThat(p -> "p1".equals(p.getId()) && p.getTrendScore() > 0));
    }
}
