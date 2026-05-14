package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
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

    // userId (email) -> online status
    private final ConcurrentHashMap<String, Boolean> onlineUsers = new ConcurrentHashMap<>();
    // userId (email) -> last seen
    private final ConcurrentHashMap<String, Date> lastSeenMap = new ConcurrentHashMap<>();

    private Object voskModel = null; // lazy init
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void initVosk() {
        try {
            var modelUrl = getClass().getResource("/vosk-model-small-fr-0.22");
            if (modelUrl == null) {
                log.warn("Vosk model not found; voice transcription disabled");
                return;
            }
            // Reflection pour éviter erreur compile si vosk absent
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

    // =========================================================
    // CONVERSATIONS 1:1
    // =========================================================

    @Override
    public ConversationResponseDTO getOrCreateConversation(String currentUserId, ConversationRequestDTO dto) {
        if (dto == null || isBlank(dto.getParticipant2Id()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le destinataire est obligatoire");

        String currentKey = resolveUserKey(currentUserId);
        String otherKey   = resolveUserKey(dto.getParticipant2Id());

        if (currentKey.equals(otherKey))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de créer une conversation avec soi-même");

        String p1 = minId(currentKey, otherKey);
        String p2 = maxId(currentKey, otherKey);

        Optional<Conversation> existing = conversationRepository.findByParticipant1IdAndParticipant2Id(p1, p2);
        if (existing.isPresent()) return mapConvResponse(existing.get(), currentKey);

        Conversation conv = Conversation.builder()
                .participant1Id(p1).participant2Id(p2)
                .avisId(dto.getAvisId())
                .groupe(false)
                .dateDernierMessage(new Date())
                .dateCreation(new Date())
                .build();
        return mapConvResponse(conversationRepository.save(conv), currentKey);
    }

    // =========================================================
    // GROUPES
    // =========================================================

    @Override
    public ConversationResponseDTO createGroupConversation(String currentUserId, GroupConversationRequestDTO dto) {
        if (dto == null || dto.getParticipantIds() == null || dto.getParticipantIds().size() < 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un groupe nécessite au moins 2 participants");

        String creatorKey = resolveUserKey(currentUserId);

        List<String> resolvedParticipants = new ArrayList<>();
        resolvedParticipants.add(creatorKey);
        for (String pid : dto.getParticipantIds()) {
            String key = resolveUserKey(pid);
            if (!resolvedParticipants.contains(key)) resolvedParticipants.add(key);
        }

        Map<String, Integer> unreadMap = new HashMap<>();
        resolvedParticipants.forEach(p -> unreadMap.put(mKey(p), 0));

        Conversation group = Conversation.builder()
                .groupe(true)
                .nomGroupe(isBlank(dto.getNomGroupe()) ? "Groupe camping" : dto.getNomGroupe())
                .avatarGroupe(dto.getAvatarGroupe())
                .participantIds(resolvedParticipants)
                .createurId(creatorKey)
                .messagesNonLusParParticipant(unreadMap)
                .dateDernierMessage(new Date())
                .dateCreation(new Date())
                .build();

        Conversation saved = conversationRepository.save(group);
        log.info("Groupe créé: {} avec {} participants", saved.getId(), resolvedParticipants.size());

        // Notifier tous les participants du nouveau groupe
        resolvedParticipants.forEach(pid ->
            messagingTemplate.convertAndSend("/topic/user/" + pid + "/notifications",
                Map.of("type", "NEW_GROUP", "groupId", saved.getId(), "nom", saved.getNomGroupe()))
        );

        return mapConvResponse(saved, creatorKey);
    }

    @Override
    public ConversationResponseDTO addParticipantToGroup(String conversationId, String userId, String newParticipantId) {
        String userKey = resolveUserKey(userId);
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (!conv.isGroupe())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette conversation n'est pas un groupe");

        if (!conv.getParticipantIds().contains(userKey))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas membre de ce groupe");

        String newKey = resolveUserKey(newParticipantId);
        if (!conv.getParticipantIds().contains(newKey)) {
            conv.getParticipantIds().add(newKey);
            conv.getMessagesNonLusParParticipant().put(mKey(newKey), 0);
            conversationRepository.save(conv);
        }
        return mapConvResponse(conv, userKey);
    }

    @Override
    public ConversationResponseDTO removeParticipantFromGroup(String conversationId, String userId, String participantId) {
        String userKey = resolveUserKey(userId);
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        if (!conv.isGroupe())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette conversation n'est pas un groupe");

        if (!conv.getCreateurId().equals(userKey) && !userKey.equals(resolveUserKey(participantId)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée");

        String targetKey = resolveUserKey(participantId);
        conv.getParticipantIds().remove(targetKey);
        conv.getMessagesNonLusParParticipant().remove(mKey(targetKey));
        conversationRepository.save(conv);
        return mapConvResponse(conv, userKey);
    }

    // =========================================================
    // MESSAGES
    // =========================================================

    @Override
    public List<ConversationResponseDTO> getUserConversations(String userId) {
        String userKey = resolveUserKey(userId);

        // Conversations 1:1
        List<Conversation> oneToOne = conversationRepository
                .findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(userKey, userKey)
                .stream().filter(c -> !c.isGroupe()).collect(Collectors.toList());

        // Groupes
        List<Conversation> groupes = conversationRepository.findAll().stream()
                .filter(c -> c.isGroupe() && c.getParticipantIds() != null && c.getParticipantIds().contains(userKey))
                .sorted(Comparator.comparing(c -> c.getDateDernierMessage() != null ? c.getDateDernierMessage() : new Date(0), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<ConversationResponseDTO> result = new ArrayList<>();
        oneToOne.forEach(c -> result.add(mapConvResponse(c, userKey)));
        groupes.forEach(c -> result.add(mapConvResponse(c, userKey)));
        result.sort(Comparator.comparing(c -> c.getDateDernierMessage() != null ? c.getDateDernierMessage() : new Date(0), Comparator.reverseOrder()));
        return result;
    }

    @Override
    public List<MessageResponseDTO> getMessages(String conversationId, int page, int size, String userId) {
        String userKey = resolveUserKey(userId);
        verifyParticipant(conversationId, userKey);
        return messageRepository.findByConversationIdOrderByDateCreationAsc(
                conversationId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)))
                .stream().map(this::mapMsgResponse).collect(Collectors.toList());
    }

    @Override
    public MessageResponseDTO sendMessage(String currentUserId, MessageRequestDTO dto) {
        if (dto == null || isBlank(dto.getConversationId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La conversation est obligatoire");
        if (isBlank(dto.getContenu()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message ne peut pas être vide");

        String senderKey = resolveUserKey(currentUserId);
        Conversation conv = verifyParticipant(dto.getConversationId(), senderKey);

        Message msg = Message.builder()
                .conversationId(dto.getConversationId())
                .expediteurId(senderKey)
                .destinataireId(conv.isGroupe() ? "GROUPE" : getRecipientId(conv, senderKey))
                .contenu(dto.getContenu().trim())
                .typeMessage(isBlank(dto.getTypeMessage()) ? "TEXT" : dto.getTypeMessage().trim().toUpperCase())
                .transcription(dto.getTranscription())
                .dateCreation(new Date())
                .build();

        Message saved = messageRepository.save(msg);
        updateConversationOnNewMessage(conv, senderKey, dto.getContenu().trim());

        MessageResponseDTO response = mapMsgResponse(saved);

        // WebSocket: diffuser à tous les participants
        messagingTemplate.convertAndSend("/topic/conversations/" + dto.getConversationId(), response);

        // Notification push par participant (badge unread)
        getParticipants(conv).stream()
                .filter(pid -> !pid.equals(senderKey))
                .forEach(pid -> messagingTemplate.convertAndSend("/topic/user/" + pid + "/notifications",
                        Map.of("type", "NEW_MESSAGE", "conversationId", conv.getId(),
                               "expediteurNom", response.getExpediteurNom())));

        return response;
    }

    @Override
    public MessageResponseDTO updateMessage(String messageId, String contenu, String userId) {
        if (isBlank(contenu))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message ne peut pas etre vide");

        String userKey = resolveUserKey(userId);
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable"));
        verifyParticipant(msg.getConversationId(), userKey);
        verifyRecentOwnMessage(msg, userKey);

        String type = msg.getTypeMessage() == null ? "TEXT" : msg.getTypeMessage();
        if (!"TEXT".equalsIgnoreCase(type))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seuls les messages texte peuvent etre modifies");

        msg.setContenu(contenu.trim());
        Message saved = messageRepository.save(msg);
        MessageResponseDTO response = mapMsgResponse(saved);
        messagingTemplate.convertAndSend("/topic/conversations/" + msg.getConversationId(),
                Map.of("eventType", "MESSAGE_UPDATED", "message", response));
        return response;
    }

    @Override
    public void deleteMessage(String messageId, String userId) {
        String userKey = resolveUserKey(userId);
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable"));
        verifyParticipant(msg.getConversationId(), userKey);
        verifyRecentOwnMessage(msg, userKey);

        String conversationId = msg.getConversationId();
        messageRepository.delete(msg);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                Map.of("eventType", "MESSAGE_DELETED", "messageId", messageId));
    }

    @Override
    public void deleteConversation(String conversationId, String userId) {
        String userKey = resolveUserKey(userId);
        verifyParticipant(conversationId, userKey);
        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId,
                Map.of("eventType", "CONVERSATION_DELETED", "conversationId", conversationId));
    }

    @Override
    public void markAsRead(String conversationId, String userId) {
        String userKey = resolveUserKey(userId);
        Conversation conv = verifyParticipant(conversationId, userKey);

        messageRepository.findByConversationIdOrderByDateCreationAsc(conversationId).stream()
                .filter(m -> userKey.equals(m.getDestinataireId()) && !m.isLu())
                .forEach(m -> { m.setLu(true); messageRepository.save(m); });

        if (conv.isGroupe()) {
            conv.getMessagesNonLusParParticipant().put(mKey(userKey), 0);
        } else {
            if (userKey.equals(conv.getParticipant1Id())) conv.setMessagesNonLusP1(0);
            else conv.setMessagesNonLusP2(0);
        }
        conversationRepository.save(conv);
    }

    // =========================================================
    // STATUT UTILISATEUR
    // =========================================================

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        if (event.getUser() == null || event.getUser().getName() == null) return;
        String userId = event.getUser().getName();
        onlineUsers.put(userId, true);
        lastSeenMap.put(userId, new Date());

        // Update MongoDB
        signupRepository.findByEmail(userId).ifPresent(u -> {
            u.setOnline(true);
            u.setLastSeen(new Date());
            signupRepository.save(u);
        });

        messagingTemplate.convertAndSend("/topic/user-status/" + userId, true);
        broadcastStatusToContacts(userId, true);
        log.info("User {} online", userId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null || event.getUser().getName() == null) return;
        if (userRegistry.getUser(event.getUser().getName()) != null) return; // encore connecté

        String userId = event.getUser().getName();
        onlineUsers.remove(userId);
        lastSeenMap.put(userId, new Date());

        signupRepository.findByEmail(userId).ifPresent(u -> {
            u.setOnline(false);
            u.setLastSeen(new Date());
            signupRepository.save(u);
        });

        messagingTemplate.convertAndSend("/topic/user-status/" + userId, false);
        broadcastStatusToContacts(userId, false);
        log.info("User {} offline", userId);
    }

    @Override
    public boolean isUserOnline(String userId) {
        String key = resolveUserKeyOptional(userId);
        return Boolean.TRUE.equals(onlineUsers.get(key));
    }

    @Override
    public UserStatusDTO getUserStatus(String userId) {
        String key = resolveUserKeyOptional(userId);
        SignupEntity user = signupRepository.findByEmail(key)
                .or(() -> signupRepository.findById(key)).orElse(null);
        return UserStatusDTO.builder()
                .userId(key)
                .nom(user != null ? getDisplayName(user) : key)
                .online(Boolean.TRUE.equals(onlineUsers.get(key)))
                .lastSeen(lastSeenMap.get(key))
                .statusMessage(user != null ? user.getStatusMessage() : null)
                .build();
    }

    @Override
    public List<UserStatusDTO> getOnlineUsers() {
        return onlineUsers.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e -> getUserStatus(e.getKey()))
                .collect(Collectors.toList());
    }

    // =========================================================
    // VOICE & APPELS
    // =========================================================

    @Override
    public String transcribeVoice(byte[] audioBytes) {
        if (voskModel == null) return "[Transcription désactivée - modèle Vosk manquant]";
        try {
            Class<?> recClass = Class.forName("org.vosk.Recognizer");
            Object rec = recClass.getConstructor(Class.forName("org.vosk.Model"), float.class)
                    .newInstance(voskModel, 16000.0f);
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            byte[] buffer = new byte[4096];
            int bytesRead;
            String result = "";
            while ((bytesRead = bais.read(buffer)) >= 0) {
                boolean accepted = (boolean) recClass.getMethod("acceptWaveForm", byte[].class, int.class)
                        .invoke(rec, buffer, bytesRead);
                if (accepted) result = (String) recClass.getMethod("getResult").invoke(rec);
            }
            if (result.isEmpty()) result = (String) recClass.getMethod("getFinalResult").invoke(rec);
            ((AutoCloseable) rec).close();
            return extractVoskText(result);
        } catch (Exception e) {
            log.error("Vosk transcription error", e);
            return "[Erreur transcription]";
        }
    }

    private String extractVoskText(String result) {
        if (isBlank(result)) return "";
        try {
            JsonNode json = objectMapper.readTree(result);
            JsonNode textNode = json.get("text");
            if (textNode != null) return textNode.asText();
        } catch (Exception ignored) {}
        return result;
    }

    @Override
    public void handleCallSignal(String conversationId, String signalData, String senderId, String callType) {
        String senderKey = resolveUserKey(senderId);
        Conversation conv = verifyParticipant(conversationId, senderKey);
        String normalizedCallType = "AUDIO".equalsIgnoreCase(callType) ? "AUDIO" : "VIDEO";

        Message signalMsg = Message.builder()
                .conversationId(conversationId)
                .expediteurId(senderKey)
                .destinataireId(conv.isGroupe() ? "GROUPE" : getRecipientId(conv, senderKey))
                .typeMessage("CALL_" + normalizedCallType + "_SIGNAL")
                .contenu(signalData)
                .callData(signalData)
                .dateCreation(new Date())
                .build();
        messageRepository.save(signalMsg);

        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/call", signalMsg);

        // Notification d'appel entrant pour tous les participants
        getParticipants(conv).stream()
                .filter(pid -> !pid.equals(senderKey))
                .forEach(pid -> messagingTemplate.convertAndSend("/topic/user/" + pid + "/notifications",
                        Map.of("type", "INCOMING_CALL", "callType", normalizedCallType,
                               "conversationId", conversationId, "from", senderKey)));
    }

    // =========================================================
    // HELPERS PRIVÉS
    // =========================================================

    private void broadcastStatusToContacts(String userId, boolean online) {
        // Notifier tous les participants des conversations de cet utilisateur
        conversationRepository.findByParticipant1IdOrParticipant2IdOrderByDateDernierMessageDesc(userId, userId)
                .forEach(conv -> getParticipants(conv).stream()
                        .filter(pid -> !pid.equals(userId))
                        .forEach(pid -> messagingTemplate.convertAndSend(
                                "/topic/user/" + pid + "/notifications",
                                Map.of("type", "USER_STATUS", "userId", userId, "online", online))));
    }

    private List<String> getParticipants(Conversation conv) {
        if (conv.isGroupe() && conv.getParticipantIds() != null) return conv.getParticipantIds();
        List<String> list = new ArrayList<>();
        if (conv.getParticipant1Id() != null) list.add(conv.getParticipant1Id());
        if (conv.getParticipant2Id() != null) list.add(conv.getParticipant2Id());
        return list;
    }

    private Conversation verifyParticipant(String conversationId, String userKey) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));
        boolean isMember = conv.isGroupe()
                ? (conv.getParticipantIds() != null && conv.getParticipantIds().contains(userKey))
                : (userKey.equals(conv.getParticipant1Id()) || userKey.equals(conv.getParticipant2Id()));
        if (!isMember)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas participant à cette conversation");
        return conv;
    }

    private void verifyRecentOwnMessage(Message msg, String userKey) {
        if (!userKey.equals(msg.getExpediteurId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez modifier que vos messages");
        if (msg.getDateCreation() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Message trop ancien");
        long ageMs = System.currentTimeMillis() - msg.getDateCreation().getTime();
        if (ageMs > 10 * 60 * 1000)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Delai de 10 minutes depasse");
    }

    private ConversationResponseDTO mapConvResponse(Conversation conv, String currentUserId) {
        ConversationResponseDTO.ConversationResponseDTOBuilder builder = ConversationResponseDTO.builder()
                .id(conv.getId())
                .groupe(conv.isGroupe())
                .avisId(conv.getAvisId())
                .dateDernierMessage(conv.getDateDernierMessage())
                .dernierMessageContenu(conv.getDernierMessageContenu());

        if (conv.isGroupe()) {
            builder.nomGroupe(conv.getNomGroupe())
                   .avatarGroupe(conv.getAvatarGroupe())
                   .participantIds(conv.getParticipantIds())
                   .participantNoms(conv.getParticipantIds() != null
                           ? conv.getParticipantIds().stream().map(this::resolveDisplayName).collect(Collectors.toList())
                           : List.of())
                   .messagesNonLus(conv.getMessagesNonLusParParticipant() != null
                           ? conv.getMessagesNonLusParParticipant().getOrDefault(mKey(currentUserId), 0)
                           : 0);
        } else {
            int unread = currentUserId.equals(conv.getParticipant1Id()) ? conv.getMessagesNonLusP1() : conv.getMessagesNonLusP2();
            String otherId = currentUserId.equals(conv.getParticipant1Id()) ? conv.getParticipant2Id() : conv.getParticipant1Id();
            builder.participant1Id(conv.getParticipant1Id())
                   .participant1Nom(resolveDisplayName(conv.getParticipant1Id()))
                   .participant2Id(conv.getParticipant2Id())
                   .participant2Nom(resolveDisplayName(conv.getParticipant2Id()))
                   .messagesNonLus(unread)
                   .autreParticipantEnLigne(Boolean.TRUE.equals(onlineUsers.get(otherId)));
        }
        return builder.build();
    }

    private MessageResponseDTO mapMsgResponse(Message msg) {
        String senderName = findUser(msg.getExpediteurId())
                .map(this::getDisplayName).orElse("Anonyme");
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .expediteurId(msg.getExpediteurId())
                .destinataireId(msg.getDestinataireId())
                .expediteurNom(senderName)
                .contenu(msg.getContenu())
                .typeMessage(msg.getTypeMessage())
                .dateCreation(msg.getDateCreation())
                .lu(msg.isLu())
                .transcription(msg.getTranscription())
                .callData(msg.getCallData())
                .build();
    }

    private void updateConversationOnNewMessage(Conversation conv, String senderId, String contenu) {
        conv.setDateDernierMessage(new Date());
        conv.setDernierMessageContenu(contenu.length() > 60 ? contenu.substring(0, 60) + "…" : contenu);
        if (conv.isGroupe()) {
            getParticipants(conv).stream()
                    .filter(pid -> !pid.equals(senderId))
                    .forEach(pid -> conv.getMessagesNonLusParParticipant()
                            .merge(mKey(pid), 1, Integer::sum));
        } else {
            if (senderId.equals(conv.getParticipant1Id())) conv.setMessagesNonLusP2(conv.getMessagesNonLusP2() + 1);
            else conv.setMessagesNonLusP1(conv.getMessagesNonLusP1() + 1);
        }
        conversationRepository.save(conv);
    }

    private String getRecipientId(Conversation conv, String senderId) {
        if (senderId.equals(conv.getParticipant1Id())) return conv.getParticipant2Id();
        if (senderId.equals(conv.getParticipant2Id())) return conv.getParticipant1Id();
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non participant");
    }

    private String resolveUserKey(String userIdOrEmail) {
        return findUser(userIdOrEmail)
                .map(SignupEntity::getEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable: " + userIdOrEmail));
    }

    /** Encode email as a MongoDB-safe map key (dots forbidden in BSON field names). */
    private String mKey(String email) {
        if (email == null) return "";
        return email.replace(".", "․").replace("$", "＄");
    }

    private String resolveUserKeyOptional(String userIdOrEmail) {
        return findUser(userIdOrEmail).map(SignupEntity::getEmail).orElse(userIdOrEmail);
    }

    private String resolveDisplayName(String key) {
        return findUser(key).map(this::getDisplayName).orElse(key);
    }

    private Optional<SignupEntity> findUser(String key) {
        if (isBlank(key)) return Optional.empty();
        return signupRepository.findByEmail(key).or(() -> signupRepository.findById(key));
    }

    private String getDisplayName(SignupEntity u) {
        String n = ((u.getFirstName() != null ? u.getFirstName() : "") + " " +
                    (u.getLastName() != null ? u.getLastName() : "")).trim();
        return n.isEmpty() ? u.getEmail() : n;
    }

    private boolean isBlank(String v) { return v == null || v.trim().isEmpty(); }
    private String minId(String a, String b) { return a.compareTo(b) < 0 ? a : b; }
    private String maxId(String a, String b) { return a.compareTo(b) > 0 ? a : b; }
}
