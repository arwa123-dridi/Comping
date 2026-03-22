package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.MessageRequestDTO;
import tn.comping.spring.backendcomping.dto.MessageResponseDTO;
import tn.comping.spring.backendcomping.entities.Message;
import tn.comping.spring.backendcomping.entities.TypeMessage;
import java.util.Date;

/**
 * Mapper Message - PHASE 3 Chat
 */
public class MessageMapper {

    public static Message toEntity(MessageRequestDTO dto, String conversationId, 
                                  String expediteurId, String destinataireId) {
        return Message.builder()
            .conversationId(conversationId)
            .expediteurId(expediteurId)
            .destinataireId(destinataireId)
            .contenu(dto.getContenu())
            .typeMessage(dto.getTypeMessage())
            .dateEnvoi(new Date())
            .lu(false)
            .build();
    }

    public static MessageResponseDTO toResponseDTO(Message message) {
        if (message == null) return null;
        
        return MessageResponseDTO.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .expediteurId(message.getExpediteurId())
            .expediteurNom("User " + message.getExpediteurId())
            .contenu(message.getContenu())
            .typeMessage(message.getTypeMessage())
            .dateEnvoi(message.getDateEnvoi())
            .lu(message.isLu())
            .dateLecture(message.getDateLecture())
            .supprime(message.isSupprime())
            .build();
    }
}
