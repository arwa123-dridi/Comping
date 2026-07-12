package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import tn.comping.spring.backendcomping.entities.NotificationType;
import tn.comping.spring.backendcomping.entities.RefType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private NotificationType type;
    private String titre;
    private String message;
    private boolean lu;
    private Date dateCreation;
    private RefType refType;
    private String refId;
    private String lien;
}
