package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.ConversationResponseDTO;
import tn.comping.spring.backendcomping.entities.Conversation;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper Conversation - PHASE 3 Chat
 */
public class ConversationMapper {

    public static ConversationResponseDTO toResponseDTO(Conversation conversation, String currentUserId) {
        if (conversation == null) return null;
        
        ConversationResponseDTO dto = ConversationResponseDTO.builder()
            .id(conversation.getId())
            .participant1Id(conversation.getParticipant1Id())
            .participant2Id(conversation.getParticipant2Id())
            .avisId(conversation.getAvisId())
            .dateCreation(conversation.getDateCreation())
            .dateDernierMessage(conversation.getDateDernierMessage())
            .active(conversation.isActive())
            .bloquee(conversation.isBloquee())
            .build();
            
        // Déterminer l'autre participant et compteur non lus
        if (conversation.getParticipant1Id().equals(currentUserId)) {
            dto.setParticipant1Nom("Moi");
            dto.setParticipant2Nom("User " + conversation.getParticipant2Id());
            dto.setMessagesNonLus(conversation.getMessagesNonLusP2());
        } else {
            dto.setParticipant1Nom("User " + conversation.getParticipant1Id());
            dto.setParticipant2Nom("Moi");
            dto.setMessagesNonLus(conversation.getMessagesNonLusP1());
        }
        
        return dto;
    }
    
    public static List<ConversationResponseDTO> toResponseDTOList(List<Conversation> conversations, String currentUserId) {
        return conversations.stream()
            .map(conv -> toResponseDTO(conv, currentUserId))
            .collect(Collectors.toList());
    }
}

