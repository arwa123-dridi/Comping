package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ConversationResponseDTO {
    
    private String id;
    
    private String participant1Id;
    private String participant1Nom;
    private String participant2Id;
    private String participant2Nom;
    
    private String avisId;              // Origine avis (optionnel)
    
    private Date dateCreation;
    private Date dateDernierMessage;
    
    private int messagesNonLus;         // Compteur pour utilisateur connecté
    
    private boolean active;
    private boolean bloquee;
}

