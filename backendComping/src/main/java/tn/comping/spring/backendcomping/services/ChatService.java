package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ConversationRequestDTO;
import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;

import java.util.List;

public interface ChatService {
    
    ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto);
    
    List<ConversationResponseDTO> getUserConversations(String userId);
    
    List<MessageResponseDTO> getMessages(String conversationId, int page, int size);
    
    MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto);
    
    void markAsRead(String conversationId, String userId);
    
}
