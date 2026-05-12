package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface ChatService {

    // === CONVERSATIONS 1:1 ===
    ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto);
    List<ConversationResponseDTO> getUserConversations(String userId);

    // === GROUPES ===
    ConversationResponseDTO createGroupConversation(String currentUserId, GroupConversationRequestDTO dto);
    ConversationResponseDTO addParticipantToGroup(String conversationId, String userId, String newParticipantId);
    ConversationResponseDTO removeParticipantFromGroup(String conversationId, String userId, String participantId);

    // === MESSAGES ===
    List<MessageResponseDTO> getMessages(String conversationId, int page, int size, String userId);
    MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto);
    void markAsRead(String conversationId, String userId);

    // === STATUT UTILISATEUR ===
    boolean isUserOnline(String userId);
    UserStatusDTO getUserStatus(String userId);
    List<UserStatusDTO> getOnlineUsers();

    // === VOICE & APPELS ===
    String transcribeVoice(byte[] audioBytes);
    void handleCallSignal(String conversationId, String signalData, String senderId, String callType);
}
