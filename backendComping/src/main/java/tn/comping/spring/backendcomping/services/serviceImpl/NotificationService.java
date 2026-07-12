package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.NotificationResponse;
import tn.comping.spring.backendcomping.entities.NotificationType;
import tn.comping.spring.backendcomping.entities.RefType;

import java.util.List;

public interface NotificationService {
    void notifyRole(String destinataireRole, NotificationType type, String titre, String message,
                     RefType refType, String refId, String lien);

    void notifyUser(String destinataireUserId, NotificationType type, String titre, String message,
                     RefType refType, String refId, String lien);

    List<NotificationResponse> getForCurrentUser(String email);

    long unreadCountForCurrentUser(String email);

    void markRead(String id, String email);

    void markAllReadForCurrentUser(String email);
}
