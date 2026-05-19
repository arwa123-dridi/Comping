package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import tn.comping.spring.backendcomping.services.ChatService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequestDTO dto) {
        // Assume security context or email in dto
        MessageResponseDTO response = chatService.sendMessage(dto.getExpediteurId(), dto);
        messagingTemplate.convertAndSend("/topic/messages." + dto.getConversationId(), response);
    }

    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload Map<String, String> payload) {
        String conversationId = payload.get("conversationId");
        String userId = payload.get("userId");
        boolean isTyping = Boolean.parseBoolean(payload.get("isTyping"));
        
        messagingTemplate.convertAndSend("/topic/typing." + conversationId, 
            Map.of("userId", userId, "isTyping", isTyping));
    }

    @MessageMapping("/chat.presence")
    public void presenceUpdate(@Payload Map<String, String> payload) {
        String conversationId = payload.get("conversationId");
        String userId = payload.get("userId");
        String status = payload.get("status"); // JOIN, LEAVE
        
        messagingTemplate.convertAndSend("/topic/presence." + conversationId, 
            Map.of("userId", userId, "status", status));
    }

    // --- WebRTC Signaling ---

    @MessageMapping("/webrtc.offer")
    public void handleOffer(@Payload Map<String, Object> payload) {
        String targetUserId = (String) payload.get("targetUserId");
        messagingTemplate.convertAndSend("/topic/webrtc." + targetUserId, payload);
    }

    @MessageMapping("/webrtc.answer")
    public void handleAnswer(@Payload Map<String, Object> payload) {
        String targetUserId = (String) payload.get("targetUserId");
        messagingTemplate.convertAndSend("/topic/webrtc." + targetUserId, payload);
    }

    @MessageMapping("/webrtc.ice")
    public void handleIceCandidate(@Payload Map<String, Object> payload) {
        String targetUserId = (String) payload.get("targetUserId");
        messagingTemplate.convertAndSend("/topic/webrtc." + targetUserId, payload);
    }

    @MessageMapping("/webrtc.hangup")
    public void handleHangup(@Payload Map<String, Object> payload) {
        String targetUserId = (String) payload.get("targetUserId");
        messagingTemplate.convertAndSend("/topic/webrtc." + targetUserId, payload);
    }
}
