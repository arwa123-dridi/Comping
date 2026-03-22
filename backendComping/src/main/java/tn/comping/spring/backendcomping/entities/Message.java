package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Message {

    @Id
    private String id;
    
    private String conversationId;
    
    private String expediteurId;
    private String destinataireId;
    
    private String contenu;             // Texte ou URL image
    private TypeMessage typeMessage;
    
    private Date dateEnvoi;
    @Builder.Default
    private boolean lu = false;
    private Date dateLecture;
    
    @Builder.Default
    private boolean supprime = false;   // Soft delete
    @Builder.Default
    private boolean signale = false;    // Signalé modération
}

