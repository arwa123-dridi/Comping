package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.*;
import tn.comping.spring.backendcomping.services.ChatService;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
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
    private final CloudinaryService cloudinaryService;

    private final ConcurrentHashMap<String, Boolean> onlineUsers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Date> lastSeenMap = new ConcurrentHashMap<>();

    private Object voskModel = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void initVosk() {
        try {
            var modelUrl = getClass().getResource("/vosk-model-fr");
            if (modelUrl == null) {
                log.warn("Vosk model not found at /vosk-model-fr; voice transcription disabled");
                return;
            }
            Class<?> libVosk = Class.forName("org.vosk.LibVosk");
            Class<?> logLevel = Class.forName("org.vosk.LogLevel");
            Object warnLevel = logLevel.getField("WARNINGS").get(null);
            libVosk.getMethod("setLogLevel", logLevel).invoke(null, warnLevel);
            Class<?> modelClass = Class.forName("org.vosk.Model");
            this.voskModel = modelClass.getConstructor(String.class)
                    .newInstance(Paths.get(modelUrl.toURI()).toString());
            log.info("Vosk model loaded successfully");
        } catch (Exception e) {
            log.warn("Vosk unavailable: {}", e.getMessage());
            this.voskModel = null;
        }
    }

    @Override
    public Map<String, String> uploadVoiceMessage(MultipartFile file) {
        try {
            String audioUrl = cloudinaryService.uploadImage(file); // Cloudinary handles audio as well
            byte[] bytes = file.getBytes();
            String transcript = transcribeVoice(bytes);
            
            return Map.of(
                "audioUrl", audioUrl,
                "transcript", transcript != null ? transcript : ""
            );
        } catch (IOException e) {
            log.error("Failed to upload voice message: {}", e.getMessage());
            throw new RuntimeException("Upload failed");
        }
    }

    @Override
    public String transcribeVoice(byte[] audioBytes) {
        if (voskModel == null) return null;
        try {
            Class<?> recognizerClass = Class.forName("org.vosk.Recognizer");
            Class<?> modelClass = Class.forName("org.vosk.Model");
            Object recognizer = recognizerClass.getConstructor(modelClass, float.class)
                    .newInstance(voskModel, 16000.0f);

            var inputStream = new ByteArrayInputStream(audioBytes);
            byte[] buffer = new byte[4096];
            int nbytes;
            while ((nbytes = inputStream.read(buffer)) >= 0) {
                recognizerClass.getMethod("acceptWaveform", byte[].class, int.class)
                        .invoke(recognizer, buffer, nbytes);
            }

            String resultJson = (String) recognizerClass.getMethod("getResult").invoke(recognizer);
            return objectMapper.readTree(resultJson).get("text").asText();
        } catch (Exception e) {
            log.error("Transcription error: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto) {
        String currentKey = resolveUserKey(currentUserId);
        String otherKey = resolveUserKey(dto.getParticipant2Id());

        if (currentKey.equals(otherKey))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Self conversation not allowed");

        String p1 = currentKey.compareTo(otherKey) < 0 ? currentKey : otherKey;
        String p2 = currentKey.compareTo(otherKey) < 0 ? otherKey : currentKey;

        Conversation conv = conversationRepository.findByParticipant1IdAndParticipant2Id(p1, p2)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .participant1Id(p1).participant2Id(p2)
                        .groupe(false)
                        .dateCreation(new Date())
                        .dateDernierMessage(new Date())
                        .build()));

        return mapConvResponse(conv, currentKey);
    }

    @Override
    public List<ConversationResponseDTO> getUserConversations(String userId) {
        String key = resolveUserKey(userId);
        return conversationRepository.findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(key, key)
                .stream()
                .map(c -> mapConvResponse(c, key))
                .toList();
    }

    @Override
    public ConversationResponseDTO createGroupConversation(String currentUserId, GroupConversationRequestDTO dto) {
        String creatorKey = resolveUserKey(currentUserId);
        List<String> participants = new ArrayList<>(dto.getParticipantIds());
        if (!participants.contains(creatorKey)) participants.add(creatorKey);

        Conversation group = Conversation.builder()
                .groupe(true)
                .nomGroupe(dto.getNomGroupe())
                .participantIds(participants)
                .createurId(creatorKey)
                .dateCreation(new Date())
                .dateDernierMessage(new Date())
                .build();

        return mapConvResponse(conversationRepository.save(group), creatorKey);
    }

    @Override
    public ConversationResponseDTO addParticipantToGroup(String conversationId, String userId, String newParticipantId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newKey = resolveUserKey(newParticipantId);
        if (!conv.getParticipantIds().contains(newKey)) {
            conv.getParticipantIds().add(newKey);
            conversationRepository.save(conv);
        }
        return mapConvResponse(conv, resolveUserKey(userId));
    }

    @Override
    public ConversationResponseDTO removeParticipantFromGroup(String conversationId, String userId, String participantId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        conv.getParticipantIds().remove(resolveUserKey(participantId));
        conversationRepository.save(conv);
        return mapConvResponse(conv, resolveUserKey(userId));
    }

    @Override
    public List<MessageResponseDTO> getMessages(String conversationId, int page, int size, String userId) {
        return messageRepository.findByConversationIdOrderByDateEnvoiDesc(conversationId, PageRequest.of(page, size))
                .stream()
                .map(this::mapMessageResponse)
                .toList();
    }

    @Override
    public MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto) {
        String key = resolveUserKey(currentUserId);
        Message message = Message.builder()
                .conversationId(dto.getConversationId())
                .expediteurId(key)
                .contenu(dto.getContenu())
                .typeMessage(dto.getType() != null ? dto.getType() : "TEXT")
                .transcription(dto.getTranscript())
                .dateCreation(new Date())
                .build();

        Message saved = messageRepository.save(message);
        
        conversationRepository.findById(dto.getConversationId()).ifPresent(c -> {
            c.setDateDernierMessage(new Date());
            c.setDernierMessageContenu(dto.getContenu());
            conversationRepository.save(c);
        });

        return mapMessageResponse(saved);
    }

    @Override
    public MessageResponseDTO updateMessage(String messageId, String contenu, String userId) {
        Message m = messageRepository.findById(messageId).orElseThrow();
        if (!m.getExpediteurId().equals(resolveUserKey(userId))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        m.setContenu(contenu);
        return mapMessageResponse(messageRepository.save(m));
    }

    @Override
    public void deleteMessage(String messageId, String userId) {
        Message m = messageRepository.findById(messageId).orElseThrow();
        if (!m.getExpediteurId().equals(resolveUserKey(userId))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        messageRepository.deleteById(messageId);
    }

    @Override
    public void deleteConversation(String conversationId, String userId) {
        conversationRepository.deleteById(conversationId);
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        // Implementation for unread count
    }

    @Override
    public boolean isUserOnline(String userId) {
        return onlineUsers.getOrDefault(resolveUserKey(userId), false);
    }

    @Override
    public UserStatusDTO getUserStatus(String userId) {
        String key = resolveUserKey(userId);
        return new UserStatusDTO(key, onlineUsers.getOrDefault(key, false), lastSeenMap.get(key));
    }

    @Override
    public List<UserStatusDTO> getOnlineUsers() {
        return onlineUsers.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e -> new UserStatusDTO(e.getKey(), true, lastSeenMap.get(e.getKey())))
                .toList();
    }

    private String resolveUserKey(String idOrEmail) {
        if (idOrEmail == null) return null;
        return signupRepository.findById(idOrEmail)
                .map(SignupEntity::getId)
                .orElseGet(() -> signupRepository.findByEmail(idOrEmail)
                        .map(SignupEntity::getId)
                        .orElse(idOrEmail));
    }

    private ConversationResponseDTO mapConvResponse(Conversation c, String currentUserId) {
        String otherId = c.isGroupe() ? null : (c.getParticipant1Id().equals(currentUserId) ? c.getParticipant2Id() : c.getParticipant1Id());
        String otherName = null;
        String otherAvatar = null;
        
        if (otherId != null) {
            SignupEntity other = signupRepository.findById(otherId).orElse(null);
            if (other != null) {
                otherName = other.getFirstName() + " " + other.getLastName();
                otherAvatar = other.getPhoto();
            }
        }

        return ConversationResponseDTO.builder()
                .id(c.getId())
                .isGroupe(c.isGroupe())
                .nomGroupe(c.getNomGroupe())
                .avatarGroupe(c.getAvatarGroupe())
                .otherParticipantId(otherId)
                .otherParticipantName(otherName)
                .otherParticipantAvatar(otherAvatar)
                .dernierMessage(c.getDernierMessage())
                .dateDernierMessage(c.getDateDernierMessage())
                .participantIds(c.getParticipantIds())
                .build();
    }

    private MessageResponseDTO mapMessageResponse(Message m) {
        return MessageResponseDTO.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .expediteurId(m.getExpediteurId())
                .contenu(m.getContenu())
                .type(m.getType())
                .audioUrl(m.getAudioUrl())
                .transcript(m.getTranscript())
                .dateEnvoi(m.getDateEnvoi())
                .build();
    }
}
