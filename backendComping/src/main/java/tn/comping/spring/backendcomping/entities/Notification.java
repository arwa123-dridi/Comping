package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    private String id;

    private String userId; // recipient
    private String actorId; // person who triggered the notification
    private String actorName;
    private String type; // FOLLOW, REACTION, COMMENT
    private String targetId; // post id or user id
    private String content;
    
    @Builder.Default
    private boolean read = false;
    
    @Builder.Default
    private Date dateCreation = new Date();
}
