package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ConversationRequestDTO;
import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;

import java.util.List;

public interface ChatService {
    
    ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto);
    
    List<ConversationResponseDTO> getUserConversations(String userId);
    
    List<MessageResponseDTO> getMessages(String conversationId, int page, int size, String userId);
    
    MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto);
    
    void markAsRead(String conversationId, String userId);
    
    // Nouvelles méthodes pour features avancées
    boolean isUserOnline(String userId);
    
    String transcribeVoice(byte[] audioBytes);
    
    void handleCallSignal(String conversationId, String signalData, String senderId, String callType);
    
}
