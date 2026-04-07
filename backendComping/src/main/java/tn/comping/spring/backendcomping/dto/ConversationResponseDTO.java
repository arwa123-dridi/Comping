package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponseDTO {
    private String id;
    private String participant1Id;
    private String participant1Nom;
    private String participant2Id;
    private String participant2Nom;
    private String avisId;
    private int messagesNonLus;
    private Date dateDernierMessage;
}
