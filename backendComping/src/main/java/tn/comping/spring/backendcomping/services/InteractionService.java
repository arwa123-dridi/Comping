package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.InteractionRequestDTO;
import tn.comping.spring.backendcomping.dto.InteractionResponseDTO;
import tn.comping.spring.backendcomping.entities.CibleType;
import java.util.List;

public interface InteractionService {
    
    /**
     * PHASE 1 - Créer un LIKE sur avis/post
     */
    InteractionResponseDTO creerLike(InteractionRequestDTO dto, String utilisateurEmail);
    
    /**
     * PHASE 1 - Créer un COMMENTAIRE sur avis/post
     */
    InteractionResponseDTO creerCommentaire(InteractionRequestDTO dto, String utilisateurEmail);
    
    /**
     * PHASE 1 - Récupérer les LIKES d'une cible
     */
    List<InteractionResponseDTO> getLikes(CibleType cibleType, String cibleId);
    
    /**
     * PHASE 1 - Récupérer les COMMENTAIRES d'une cible (triés)
     */
    List<InteractionResponseDTO> getCommentaires(CibleType cibleType, String cibleId);
    
    /**
     * PHASE 1 - Supprimer son LIKE
     */
    void supprimerLike(String interactionId, String utilisateurEmail);
    
    /**
     * PHASE 1 - Supprimer son COMMENTAIRE  
     */
    void supprimerCommentaire(String interactionId, String utilisateurEmail);
    
    /**
     * Compter les likes d'une cible (pour AvisService)
     */
    long compterLikes(CibleType cibleType, String cibleId);
}

