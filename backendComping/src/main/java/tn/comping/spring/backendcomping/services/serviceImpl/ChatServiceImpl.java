package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.services.ChatService;
import tn.comping.spring.backendcomping.utils.mapper.ChatMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SignupRepository signupRepository;

    @Override
    public ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto) {
        String otherUserId = dto.getParticipant2Id();
        String avisId = dto.getAvisId();

        // Ensure unique 1:1, normalize order
        String p1 = minId(currentUserId, otherUserId);
        String p2 = maxId(currentUserId, otherUserId);

        var convOpt = conversationRepository.findByParticipant1IdAndParticipant2Id(p1, p2);
        if (convOpt.isEmpty()) {
            Conversation newConv = Conversation.builder()
                    .participant1Id(p1)
                    .participant2Id(p2)
                    .avisId(avisId)
                    .build();
            Conversation saved = conversationRepository.save(newConv);
            return mapConvResponse(saved);
        }
        return mapConvResponse(convOpt.get());
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(String userId) {
        return conversationRepository.findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(userId, userId)
                .stream()
                .map(this::mapConvResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponseDTO> getMessages(String conversationId, int page, int size) {
        return messageRepository.findByConversationIdOrderByDateCreationAsc(conversationId)
                .stream()
                .skip(page * size)
                .limit(size)
                .map(this::mapMsgResponse)
                .collect(Collectors.toList());
    }

@Override
    public MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto) {
        Message msg = ChatMapper.toEntity(dto, currentUserId);
        Message saved = messageRepository.save(msg);

        // Update conv unread & last msg date
        updateConversationOnNewMessage(dto.getConversationId(), currentUserId);

        log.info("Message sent in conv {} by user {}", dto.getConversationId(), currentUserId);
        MessageResponseDTO response = mapMsgResponse(saved);
        return response;
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        // Logic to set lu=true for user's unread msgs, reset unread count
        // Implementation details...
    }

    private String minId(String a, String b) { return a.compareTo(b) < 0 ? a : b; }
    private String maxId(String a, String b) { return a.compareTo(b) > 0 ? a : b; }

    private ConversationResponseDTO mapConvResponse(Conversation conv) {
        // Fetch user names, unread for current user, etc.
        // Simplified for now
        return ConversationResponseDTO.builder()
                .id(conv.getId())
                .participant1Id(conv.getParticipant1Id())
                // ... fill names from signupRepo
                .build();
    }

    private MessageResponseDTO mapMsgResponse(Message msg) {
        // Fetch expediteurNom from signupRepo
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .expediteurNom("User") // fetch real
                .build();
    }

    private void updateConversationOnNewMessage(String convId, String senderId) {
        // Increment unread for recipient, update dateDernierMessage
        // Implementation...
    }
}
