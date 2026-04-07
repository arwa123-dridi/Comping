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
    
    private String contenu;
    private String typeMessage; // TEXT, IMAGE, etc.
    
    private boolean lu = false;
    private boolean supprime = false;
    
    private Date dateCreation;
}
