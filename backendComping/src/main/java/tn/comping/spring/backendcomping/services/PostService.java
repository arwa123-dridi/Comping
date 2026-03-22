package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import java.util.List;

public interface PostService {
    
    /**
     * PHASE 2 - Créer un post (texte/images)
     */
    PostResponseDTO creerPost(PostRequestDTO dto, String utilisateurEmail);
    
    /**
     * PHASE 2 - Modifier son post
     */
    PostResponseDTO modifierPost(String id, PostRequestDTO dto, String utilisateurEmail);
    
    /**
     * PHASE 2 - Supprimer son post
     */
    void supprimerPost(String id, String utilisateurEmail);
    
    /**
     * PHASE 2 - Posts d'un utilisateur
     */
    List<PostResponseDTO> getPostsUtilisateur(String utilisateurId);
    
    /**
     * PHASE 2 - Tous les posts publics récents
     */
    List<PostResponseDTO> getPostsPublics();
    
    /**
     * PHASE 2 - Partager un avis comme post
     */
    PostResponseDTO partagerAvis(String avisId, String utilisateurEmail);
}

