package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import java.util.List;

public interface ChatService {
    
    /**
     * PHASE 3 - Créer conversation entre 2 users (réutilise si existe)
     */
    ConversationResponseDTO creerConversation(String autreUserId, String currentUserEmail, String avisId);
    
    /**
     * PHASE 3 - Créer conversation depuis avis
     */
    ConversationResponseDTO creerConversationDepuisAvis(String avisId, String currentUserEmail);
    
    /**
     * PHASE 3 - Lister conversations utilisateur
     */
    List<ConversationResponseDTO> getMesConversations(String currentUserEmail);
    
    /**
     * PHASE 3 - Envoyer message
     */
    MessageResponseDTO envoyerMessage(String conversationId, MessageRequestDTO dto, String expediteurEmail);
    
    /**
     * PHASE 3 - Historique messages
     */
    List<MessageResponseDTO> getMessagesConversation(String conversationId);
    
    /**
     * PHASE 3 - Marquer messages lus
     */
    void marquerLu(String conversationId, String destinataireEmail);
    
    /**
     * PHASE 3 - Supprimer conversation (soft)
     */
    void supprimerConversation(String conversationId, String userEmail);
}

