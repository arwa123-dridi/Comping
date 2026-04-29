package tn.comping.spring.backendcomping.config;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import java.util.Date;


@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public MessageResponseDTO sendMessage(MessageRequestDTO message) {
        // Message WebSocket direct - bypass REST
        // Logic simplified: forward to all subscribers
        return MessageResponseDTO.builder()
                .id("ws-" + System.currentTimeMillis())
                .contenu(message.getContenu())
                .conversationId(message.getConversationId())
                .expediteurNom("WebSocket User")
                .dateCreation(new Date())
                .build();
    }

    @MessageMapping("/chat.private")
    public void sendPrivate(MessageRequestDTO message) {
        // Send to conversation participants
        messagingTemplate.convertAndSend("/topic/conversations/" + message.getConversationId(), message);
    }
}
