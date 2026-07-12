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
@ToString
public class Notification {

    @Id
    private String id;
    private String destinataireUserId;
    private String destinataireRole;
    private NotificationType type;
    private String titre;
    private String message;
    private boolean lu;
    private Date dateCreation;
    private RefType refType;
    private String refId;
    private String lien;
}
