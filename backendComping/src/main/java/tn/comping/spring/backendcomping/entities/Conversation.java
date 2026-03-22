package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Conversation {
    
    @Id
    private String id;
    
    private String participant1Id;      // User1 ID
    private String participant2Id;      // User2 ID
    
    private String avisId;              // Origine conversation (optionnel)
    
    private Date dateCreation;
    private Date dateDernierMessage;
    
    @Builder.Default
    private int messagesNonLusP1 = 0;  // Compteur non lus pour participant1
    @Builder.Default
    private int messagesNonLusP2 = 0;  // Compteur non lus pour participant2
    
    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private boolean bloquee = false;
}
