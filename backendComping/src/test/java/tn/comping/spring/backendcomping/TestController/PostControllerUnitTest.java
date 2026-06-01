package tn.comping.spring.backendcomping.TestController;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import tn.comping.spring.backendcomping.controllers.PostController;
import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.dto.ReactionRequestDTO;
import tn.comping.spring.backendcomping.services.AbonnementService;
import tn.comping.spring.backendcomping.services.PostService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires (Mockito pur) - PostController")
class PostControllerUnitTest {

    @Mock private PostService postService;
    @Mock private AbonnementService abonnementService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private PostController postController;

    private Authentication auth;
    private PostResponseDTO postResponse;
    private PostRequestDTO postRequest;

    @BeforeEach
    void setUp() {
        auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("auteur@test.com");

        postResponse = PostResponseDTO.builder()
                .id("post1").auteurId("auteur@test.com").auteurNom("Jean Dupont")
                .contenu("Mon super post #camping").hashtags(List.of("camping"))
                .datePublication(new Date()).likesCount(0).commentairesCount(0)
                .reactions(new HashMap<>()).visibilite("PUBLIC").trendScore(0.0)
                .images(List.of()).build();

        postRequest = PostRequestDTO.builder()
                .contenu("Mon super post #camping").visibilite("PUBLIC").build();
    }

    // =========================================================
    // POST /api/posts  →  createPost (JSON)
    // =========================================================

    @Test
    @DisplayName("createPost : délègue au service et retourne 200 OK")
    void createPost_delegueAuService_retourne200() {
        when(postService.createPost(any(PostRequestDTO.class), eq("auteur@test.com")))
                .thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response = postController.createPost(postRequest, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContenu()).isEqualTo("Mon super post #camping");
        verify(postService).createPost(postRequest, "auteur@test.com");
    }

    @Test
    @DisplayName("createPost : service lève 400 → exception propagée")
    void createPost_serviceLance400_propagee() {
        when(postService.createPost(any(), eq("auteur@test.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contenu obligatoire"));

        assertThatThrownBy(() -> postController.createPost(postRequest, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    @DisplayName("createPost : email extrait de Authentication")
    void createPost_extraitEmailDepuisAuth() {
        when(postService.createPost(any(), eq("auteur@test.com"))).thenReturn(postResponse);

        postController.createPost(postRequest, auth);

        verify(auth).getName();
        verify(postService).createPost(any(), eq("auteur@test.com"));
    }

    // =========================================================
    // GET /api/posts/feed  →  getFeed
    // =========================================================

    @Test
    @DisplayName("getFeed : délègue au service avec page et size par défaut")
    void getFeed_delegueAvecParametres_retourne200() {
        when(postService.getFeedPosts(0, 20, "auteur@test.com"))
                .thenReturn(List.of(postResponse));

        ResponseEntity<List<PostResponseDTO>> response =
                postController.getFeed(0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(postService).getFeedPosts(0, 20, "auteur@test.com");
    }

    @Test
    @DisplayName("getFeed : retourne liste vide → 200 OK")
    void getFeed_listeVide_retourne200() {
        when(postService.getFeedPosts(0, 20, "auteur@test.com")).thenReturn(List.of());

        ResponseEntity<List<PostResponseDTO>> response = postController.getFeed(0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    // GET /api/posts/trending  →  getTrending
    // =========================================================

    @Test
    @DisplayName("getTrending : délègue au service et retourne posts trending")
    void getTrending_delegueAuService_retourne200() {
        PostResponseDTO trending = PostResponseDTO.builder()
                .id("t1").contenu("Post viral").trendScore(95.0).build();
        when(postService.getTrendingPosts(0, 20, "auteur@test.com"))
                .thenReturn(List.of(trending));

        ResponseEntity<List<PostResponseDTO>> response = postController.getTrending(0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getTrendScore()).isEqualTo(95.0);
        verify(postService).getTrendingPosts(0, 20, "auteur@test.com");
    }

    // =========================================================
    // GET /api/posts/amis  →  getAmisPosts
    // =========================================================

    @Test
    @DisplayName("getAmisPosts : récupère abonnements puis posts des amis")
    void getAmisPosts_recupereAbonnementsEtPosts() {
        AbonnementResponseDTO abonnement = AbonnementResponseDTO.builder()
                .suiviId("ami@test.com").build();
        when(abonnementService.getMesAbonnements("auteur@test.com"))
                .thenReturn(List.of(abonnement));
        when(postService.getFriendsPosts(List.of("ami@test.com"), 0, 20, "auteur@test.com"))
                .thenReturn(List.of(postResponse));

        ResponseEntity<List<PostResponseDTO>> response = postController.getAmisPosts(0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(abonnementService).getMesAbonnements("auteur@test.com");
        verify(postService).getFriendsPosts(List.of("ami@test.com"), 0, 20, "auteur@test.com");
    }

    @Test
    @DisplayName("getAmisPosts : aucun abonnement → posts avec liste vide d'amis")
    void getAmisPosts_sansAbonnements_postsAvecListeVide() {
        when(abonnementService.getMesAbonnements("auteur@test.com")).thenReturn(List.of());
        when(postService.getFriendsPosts(List.of(), 0, 20, "auteur@test.com"))
                .thenReturn(List.of());

        ResponseEntity<List<PostResponseDTO>> response = postController.getAmisPosts(0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    // =========================================================
    // GET /api/posts/hashtag/{hashtag}  →  getByHashtag
    // =========================================================

    @Test
    @DisplayName("getByHashtag : délègue au service avec le hashtag")
    void getByHashtag_delegueAvecHashtag() {
        when(postService.getPostsByHashtag("camping", 0, 20, "auteur@test.com"))
                .thenReturn(List.of(postResponse));

        ResponseEntity<List<PostResponseDTO>> response =
                postController.getByHashtag("camping", 0, 20, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).getPostsByHashtag("camping", 0, 20, "auteur@test.com");
    }

    // =========================================================
    // GET /api/posts/{id}  →  getPost
    // =========================================================

    @Test
    @DisplayName("getPost : délègue au service et retourne le post")
    void getPost_delegueAuService_retournePost() {
        when(postService.getPostById("post1", "auteur@test.com")).thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response = postController.getPost("post1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("post1");
        verify(postService).getPostById("post1", "auteur@test.com");
    }

    @Test
    @DisplayName("getPost : ID inexistant → exception propagée")
    void getPost_idInexistant_exceptionPropagee() {
        when(postService.getPostById("inexistant", "auteur@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouvé"));

        assertThatThrownBy(() -> postController.getPost("inexistant", auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // PUT /api/posts/{id}  →  updatePost (JSON)
    // =========================================================

    @Test
    @DisplayName("updatePost : délègue au service et retourne post mis à jour")
    void updatePost_delegueAuService_retournePostMisAJour() {
        PostResponseDTO postMaj = PostResponseDTO.builder()
                .id("post1").contenu("Contenu modifié").visibilite("AMIS").build();
        when(postService.updatePost(eq("post1"), any(PostRequestDTO.class), eq("auteur@test.com")))
                .thenReturn(postMaj);

        ResponseEntity<PostResponseDTO> response =
                postController.updatePost("post1", postRequest, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContenu()).isEqualTo("Contenu modifié");
        verify(postService).updatePost("post1", postRequest, "auteur@test.com");
    }

    @Test
    @DisplayName("updatePost : non propriétaire → exception 403 propagée")
    void updatePost_nonProprietaire_exception403Propagee() {
        when(postService.updatePost(eq("post1"), any(), eq("auteur@test.com")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé"));

        assertThatThrownBy(() -> postController.updatePost("post1", postRequest, auth))
                .isInstanceOf(ResponseStatusException.class);
    }

    // =========================================================
    // DELETE /api/posts/{id}  →  deletePost
    // =========================================================

    @Test
    @DisplayName("deletePost : délègue au service et retourne 204")
    void deletePost_delegueAuService_retourne204() {
        doNothing().when(postService).deletePost("post1", "auteur@test.com");

        ResponseEntity<Void> response = postController.deletePost("post1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(postService).deletePost("post1", "auteur@test.com");
    }

    // =========================================================
    // POST /api/posts/{id}/like  →  likePost
    // =========================================================

    @Test
    @DisplayName("likePost : appelle likePost puis getPostById")
    void likePost_appelle_likeEtGetPostById() {
        doNothing().when(postService).likePost("post1", "auteur@test.com");
        when(postService.getPostById("post1", "auteur@test.com")).thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response = postController.likePost("post1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).likePost("post1", "auteur@test.com");
        verify(postService).getPostById("post1", "auteur@test.com");
    }

    // =========================================================
    // DELETE /api/posts/{id}/like  →  unlikePost
    // =========================================================

    @Test
    @DisplayName("unlikePost : appelle unlikePost puis getPostById")
    void unlikePost_appelle_unlikeEtGetPostById() {
        doNothing().when(postService).unlikePost("post1", "auteur@test.com");
        when(postService.getPostById("post1", "auteur@test.com")).thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response = postController.unlikePost("post1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).unlikePost("post1", "auteur@test.com");
    }

    // =========================================================
    // POST /api/posts/{id}/react  →  reactToPost
    // =========================================================

    @Test
    @DisplayName("reactToPost : appelle reactToPost avec emoji et retourne post mis à jour")
    void reactToPost_appelle_reactEtGetPostById() {
        ReactionRequestDTO reactionDto = new ReactionRequestDTO();
        reactionDto.setEmoji("❤️");
        doNothing().when(postService).reactToPost("post1", "auteur@test.com", "❤️");
        when(postService.getPostById("post1", "auteur@test.com")).thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response =
                postController.reactToPost("post1", reactionDto, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).reactToPost("post1", "auteur@test.com", "❤️");
    }

    // =========================================================
    // DELETE /api/posts/{id}/react  →  removeReaction
    // =========================================================

    @Test
    @DisplayName("removeReaction : supprime la réaction et retourne post mis à jour")
    void removeReaction_supprimeEtRetournePost() {
        doNothing().when(postService).removeReaction("post1", "auteur@test.com");
        when(postService.getPostById("post1", "auteur@test.com")).thenReturn(postResponse);

        ResponseEntity<PostResponseDTO> response = postController.removeReaction("post1", auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(postService).removeReaction("post1", "auteur@test.com");
    }
}
