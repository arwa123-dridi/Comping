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
    private String expediteurNom;
    private String contenu;
    private String typeMessage;
    private boolean lu;
    private Date dateCreation;
}
