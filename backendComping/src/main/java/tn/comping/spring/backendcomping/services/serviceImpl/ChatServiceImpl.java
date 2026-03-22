package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.entities.Message;
import tn.comping.spring.backendcomping.repositories.ConversationRepository;
import tn.comping.spring.backendcomping.repositories.MessageRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.ChatService;
import tn.comping.spring.backendcomping.utils.mapper.ConversationMapper;
import tn.comping.spring.backendcomping.utils.mapper.MessageMapper;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SignupRepository signupRepository;

    @Override
    public ConversationResponseDTO creerConversation(String autreUserId, String currentUserEmail, String avisId) {
        String currentUserId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        // Vérifier si conversation existe déjà (règle métier 3)
        conversationRepository.findByParticipant1IdAndParticipant2IdOrParticipant2IdAndParticipant1Id(
                currentUserId, autreUserId, autreUserId, currentUserId)
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Conversation existe déjà");
            });

        Conversation conversation = Conversation.builder()
            .participant1Id(currentUserId)
            .participant2Id(autreUserId)
            .avisId(avisId)
            .dateCreation(new Date())
            .build();

        conversation = conversationRepository.save(conversation);
        log.info("Conversation créée ID: {} entre {} et {}", conversation.getId(), currentUserId, autreUserId);
        
        return ConversationMapper.toResponseDTO(conversation, currentUserId);
    }

    @Override
    public ConversationResponseDTO creerConversationDepuisAvis(String avisId, String currentUserEmail) {
        // TODO: Logique depuis avis (propriétaire + auteur avis ?)
        return null; // Implémenter
    }

    @Override
    public List<ConversationResponseDTO> getMesConversations(String currentUserEmail) {
        String currentUserId = signupRepository.findByEmail(currentUserEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        List<Conversation> conversations = conversationRepository
            .findByParticipant1IdAndActiveOrParticipant2IdAndActiveOrderByDateDernierMessageDesc(
                currentUserId, true, currentUserId, true);
                
        return ConversationMapper.toResponseDTOList(conversations, currentUserId);
    }

    @Override
    public MessageResponseDTO envoyerMessage(String conversationId, MessageRequestDTO dto, String expediteurEmail) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));

        String currentUserId = signupRepository.findByEmail(expediteurEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        // Vérifier autorisation (participant)
        if (!conversation.getParticipant1Id().equals(currentUserId) && 
            !conversation.getParticipant2Id().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé dans cette conversation");
        }

        String destinataireId = conversation.getParticipant1Id().equals(currentUserId) 
            ? conversation.getParticipant2Id() : conversation.getParticipant1Id();

        Message message = MessageMapper.toEntity(dto, conversationId, currentUserId, destinataireId);
        message = messageRepository.save(message);

        // Update conversation
        conversation.setDateDernierMessage(message.getDateEnvoi());
        if (conversation.getParticipant1Id().equals(currentUserId)) {
            conversation.setMessagesNonLusP2(conversation.getMessagesNonLusP2() + 1);
        } else {
            conversation.setMessagesNonLusP1(conversation.getMessagesNonLusP1() + 1);
        }
        conversationRepository.save(conversation);

        log.info("Message envoyé conversation: {} id: {}", conversationId, message.getId());
        return MessageMapper.toResponseDTO(message);
    }

    @Override
    public List<MessageResponseDTO> getMessagesConversation(String conversationId) {
        List<Message> messages = messageRepository
            .findByConversationIdAndSupprimeFalseOrderByDateEnvoiAsc(conversationId);
        return messages.stream()
            .map(MessageMapper::toResponseDTO)
            .toList();
    }

    @Override
    public void marquerLu(String conversationId, String destinataireEmail) {
        String destinataireId = signupRepository.findByEmail(destinataireEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        List<Message> nonLus = messageRepository
            .findByConversationIdAndDestinataireIdAndLuFalseAndSupprimeFalse(conversationId, destinataireId);
            
        nonLus.forEach(msg -> {
            msg.setLu(true);
            msg.setDateLecture(new Date());
            messageRepository.save(msg);
        });

        // Reset compteur non lus
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));
            
        if (conversation.getParticipant1Id().equals(destinataireId)) {
            conversation.setMessagesNonLusP1(0);
        } else {
            conversation.setMessagesNonLusP2(0);
        }
        conversationRepository.save(conversation);
        
        log.info("Messages marqués lus conversation: {} user: {}", conversationId, destinataireId);
    }

    @Override
    public void supprimerConversation(String conversationId, String userEmail) {
        String userId = signupRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"))
            .getId();

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée"));

        if (!conversation.getParticipant1Id().equals(userId) && 
            !conversation.getParticipant2Id().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        conversation.setActive(false);
        conversationRepository.save(conversation);
        
        log.info("Conversation supprimée (soft) ID: {}", conversationId);
    }
}

