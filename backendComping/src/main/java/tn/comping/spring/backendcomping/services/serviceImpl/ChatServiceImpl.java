package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.services.ChatService;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.ChatMapper;

import jakarta.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SignupRepository signupRepository;
    private final SimpUserRegistry userRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    
    private final ConcurrentHashMap<String, Boolean> onlineUsers = new ConcurrentHashMap<>();
    private Model voskModel;
    
    @PostConstruct
    private void initVosk() {
        try {
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            // Model depuis resources (télécharger vosk-model-small-fr-0.22.zip)
            var modelUrl = getClass().getResource("/vosk-model-small-fr-0.22");
            if (modelUrl == null) {
                log.warn("Vosk model not found in resources; voice transcription disabled");
                return;
            }
            this.voskModel = new Model(Paths.get(modelUrl.toURI()).toString());

        } catch (Exception e) {
            log.warn("Unable to initialize Vosk model; voice transcription disabled", e);
            this.voskModel = null;
        }
    }

    @Override
    public ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto) {
        if (dto == null || isBlank(dto.getParticipant2Id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le destinataire est obligatoire");
        }

        String currentUserKey = resolveUserKey(currentUserId);
        String otherUserId = resolveUserKey(dto.getParticipant2Id());
        String avisId = dto.getAvisId();

        if (currentUserKey.equals(otherUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de creer une conversation avec soi-meme");
        }

        String p1 = minId(currentUserKey, otherUserId);
        String p2 = maxId(currentUserKey, otherUserId);

        var convOpt = conversationRepository.findByParticipant1IdAndParticipant2Id(p1, p2);
        if (convOpt.isEmpty()) {
            Conversation newConv = Conversation.builder()
                    .participant1Id(p1)
                    .participant2Id(p2)
                    .avisId(avisId)
                    .dateDernierMessage(new Date())
                    .build();
            Conversation saved = conversationRepository.save(newConv);
            return mapConvResponse(saved, currentUserKey);
        }
        return mapConvResponse(convOpt.get(), currentUserKey);
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(String userId) {
        String userKey = resolveUserKey(userId);
        return conversationRepository.findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(userKey, userKey)
                .stream()
                .map(conversation -> mapConvResponse(conversation, userKey))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponseDTO> getMessages(String conversationId, int page, int size, String userId) {
        String userKey = resolveUserKey(userId);
        Conversation conversation = getConversationForParticipant(conversationId, userKey);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        return messageRepository.findByConversationIdOrderByDateCreationAsc(
                        conversation.getId(),
                        PageRequest.of(safePage, safeSize))
                .stream()
                .map(this::mapMsgResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto) {
        if (dto == null || isBlank(dto.getConversationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La conversation est obligatoire");
        }
        if (isBlank(dto.getContenu())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message ne peut pas etre vide");
        }

        String senderId = resolveUserKey(currentUserId);
        Conversation conversation = getConversationForParticipant(dto.getConversationId(), senderId);
        Message msg = Message.builder()
                .conversationId(dto.getConversationId())
                .expediteurId(senderId)
                .destinataireId(getRecipientId(conversation, senderId))
                .contenu(dto.getContenu().trim())
                .typeMessage(isBlank(dto.getTypeMessage()) ? "TEXT" : dto.getTypeMessage().trim().toUpperCase())
                .transcription(dto.getTranscription())
                .dateCreation(new Date())
                .build();
        Message saved = messageRepository.save(msg);

        updateConversationOnNewMessage(conversation, senderId);

        log.info("Message sent in conv {} by user {}", dto.getConversationId(), senderId);
        MessageResponseDTO response = mapMsgResponse(saved);
        messagingTemplate.convertAndSend("/topic/conversations/" + dto.getConversationId(), response);
        return response;
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        String userKey = resolveUserKey(userId);
        Conversation conversation = getConversationForParticipant(conversationId, userKey);

        messageRepository.findByConversationIdOrderByDateCreationAsc(conversationId).stream()
                .filter(message -> userKey.equals(message.getDestinataireId()))
                .filter(message -> !message.isLu())
                .forEach(message -> {
                    message.setLu(true);
                    messageRepository.save(message);
                });

        if (userKey.equals(conversation.getParticipant1Id())) {
            conversation.setMessagesNonLusP1(0);
        } else if (userKey.equals(conversation.getParticipant2Id())) {
            conversation.setMessagesNonLusP2(0);
        }
        conversationRepository.save(conversation);
    }

    private String minId(String a, String b) { return a.compareTo(b) < 0 ? a : b; }
    private String maxId(String a, String b) { return a.compareTo(b) > 0 ? a : b; }

    private ConversationResponseDTO mapConvResponse(Conversation conv, String currentUserId) {
        String participant1Name = findUser(conv.getParticipant1Id())
                .map(this::getDisplayName).orElse("Inconnu");
        String participant2Name = findUser(conv.getParticipant2Id())
                .map(this::getDisplayName).orElse("Inconnu");
        int messagesNonLus = currentUserId.equals(conv.getParticipant1Id())
                ? conv.getMessagesNonLusP1()
                : conv.getMessagesNonLusP2();
        
        return ConversationResponseDTO.builder()
                .id(conv.getId())
                .participant1Id(conv.getParticipant1Id())
                .participant1Nom(participant1Name)
                .participant2Id(conv.getParticipant2Id())
                .participant2Nom(participant2Name)
                .avisId(conv.getAvisId())
                .messagesNonLus(messagesNonLus)
                .dateDernierMessage(conv.getDateDernierMessage())
                .build();
    }

    private MessageResponseDTO mapMsgResponse(Message msg) {
        String senderName = findUser(msg.getExpediteurId())
                .map(this::getDisplayName).orElse("Anonyme");
        
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .expediteurNom(senderName)
                .contenu(msg.getContenu())
                .typeMessage(msg.getTypeMessage())
                .dateCreation(msg.getDateCreation())
                .lu(msg.isLu())
                .transcription(msg.getTranscription())
                .build();
    }

    private String getDisplayName(SignupEntity user) {
        String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }

    private void updateConversationOnNewMessage(Conversation conversation, String senderId) {
        if (senderId.equals(conversation.getParticipant1Id())) {
            conversation.setMessagesNonLusP2(conversation.getMessagesNonLusP2() + 1);
        } else if (senderId.equals(conversation.getParticipant2Id())) {
            conversation.setMessagesNonLusP1(conversation.getMessagesNonLusP1() + 1);
        }
        conversation.setDateDernierMessage(new Date());
        conversationRepository.save(conversation);
    }

    private String getRecipientId(Conversation conversation, String senderId) {
        if (senderId.equals(conversation.getParticipant1Id())) {
            return conversation.getParticipant2Id();
        }
        if (senderId.equals(conversation.getParticipant2Id())) {
            return conversation.getParticipant1Id();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utilisateur non participant a cette conversation");
    }

    private Conversation getConversationForParticipant(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));
        getRecipientId(conversation, userId);
        return conversation;
    }

    private String resolveUserKey(String userIdOrEmail) {
        return findUser(userIdOrEmail)
                .map(SignupEntity::getEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private Optional<SignupEntity> findUser(String userIdOrEmail) {
        if (isBlank(userIdOrEmail)) {
            return Optional.empty();
        }
        return signupRepository.findByEmail(userIdOrEmail)
                .or(() -> signupRepository.findById(userIdOrEmail));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    // === STATUS ONLINE ===
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        var user = event.getUser();
        if (user != null && user.getName() != null) {
            String userId = user.getName();
            onlineUsers.put(userId, true);
            messagingTemplate.convertAndSend("/topic/user-status/" + userId, true);
            log.info("User {} online", userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var user = event.getUser();
        if (user != null && user.getName() != null && userRegistry.getUser(user.getName()) == null) {
            String userId = user.getName();
            onlineUsers.remove(userId);
            messagingTemplate.convertAndSend("/topic/user-status/" + userId, false);
            log.info("User {} offline", userId);
        }
    }
    
    public boolean isUserOnline(String userId) {
        return Boolean.TRUE.equals(onlineUsers.get(userId));
    }
    
    // === VOICE TRANSCRIPTION VOSK ===
    public String transcribeVoice(byte[] audioBytes) {
        if (voskModel == null) return "[Transcription désactivée - model manquant]";
        
        try (Recognizer recognizer = new Recognizer(voskModel, 16000.0f)) {
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bais.read(buffer)) >= 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    return recognizer.getResult();
                }
            }
            return recognizer.getFinalResult();
        } catch (IOException e) {
            log.error("Transcription error", e);
            return "[Erreur transcription]";
        }
    }
    
    // === WEBRTC SIGNALING (simplifié) ===
    public void handleCallSignal(String conversationId, String signalData, String senderId, String callType) {
        String senderKey = resolveUserKey(senderId);
        Conversation conversation = getConversationForParticipant(conversationId, senderKey);
        String normalizedCallType = "AUDIO".equalsIgnoreCase(callType) ? "AUDIO" : "VIDEO";
        String recipientId = getRecipientId(conversation, senderKey);

        Message signalMsg = Message.builder()
                .conversationId(conversationId)
                .expediteurId(senderKey)
                .destinataireId(recipientId)
                .typeMessage("CALL_" + normalizedCallType + "_SIGNAL")
                .contenu(signalData)
                .callData(signalData)
                .dateCreation(new Date())
                .build();
        messageRepository.save(signalMsg);
        
        // Broadcast to convo participants
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/call", signalMsg);
    }
}
