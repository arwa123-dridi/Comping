package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.entities.Notification;
import java.util.List;

public interface NotificationService {
    Notification createNotification(String userId, String actorId, String type, String targetId, String content);
    List<Notification> getNotificationsForUser(String userId);
    void markAsRead(String id);
    long getUnreadCount(String userId);
}
