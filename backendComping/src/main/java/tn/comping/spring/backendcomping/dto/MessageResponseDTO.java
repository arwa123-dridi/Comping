package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {
    private String id;
    private String conversationId;
    private String expediteurId;
    private String destinataireId;
    private String expediteurNom;
    private String contenu;
    private String typeMessage;
    private boolean lu;
    private Date dateCreation;
    private String transcription; // Pour voice messages
    private String callData;
}
