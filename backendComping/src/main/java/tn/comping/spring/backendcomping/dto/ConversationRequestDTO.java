package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequestDTO {
    private String participant2Id; // Current user is participant1
    private String avisId; // Optional, chat from avis
}
