package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.entities.Message;

import java.util.Date;

public class ChatMapper {

    public static Message toEntity(MessageRequestDTO dto, String expediteurId) {
        if (dto == null) return null;
        
        return Message.builder()
                .conversationId(dto.getConversationId())
                .expediteurId(expediteurId)
                .destinataireId(getDestinataireId(dto.getConversationId(), expediteurId)) // Logic to derive
                .contenu(dto.getContenu())
                .typeMessage(dto.getTypeMessage() != null ? dto.getTypeMessage() : "TEXT")
                .dateCreation(new Date())
                .build();
    }
    
    private static String getDestinataireId(String convId, String senderId) {
        // Extract from conversation entity lookup (simplified)
        return "other_user"; // Implement properly
    }
    
}
