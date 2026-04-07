package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Conversation {
    @Id
    private String id;

    private String participant1Id;
    private String participant2Id;
    
    private String avisId; // Optional link to Avis (if chat from review)
    
    private int messagesNonLusP1 = 0;
    private int messagesNonLusP2 = 0;
    
    private Date dateDernierMessage;
    
    // Normalized for unique 1:1 (participant1Id always < participant2Id lexicographically)
}
