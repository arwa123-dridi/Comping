package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeMessage;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MessageResponseDTO {
    
    private String id;
    private String conversationId;
    
    private String expediteurId;
    private String expediteurNom;
    
    private String contenu;
    private TypeMessage typeMessage;
    
    private Date dateEnvoi;
    private boolean lu;
    private Date dateLecture;
    
    private boolean supprime;
}

