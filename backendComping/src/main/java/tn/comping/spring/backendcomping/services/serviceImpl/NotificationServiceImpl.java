package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.entities.Notification;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.NotificationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SignupRepository signupRepository;

    @Override
    public Notification createNotification(String userId, String actorId, String type, String targetId, String content) {
        if (userId.equals(actorId)) return null; // Don't notify self

        String actorName = signupRepository.findById(actorId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Quelqu'un");

        Notification notification = Notification.builder()
                .userId(userId)
                .actorId(actorId)
                .actorName(actorName)
                .type(type)
                .targetId(targetId)
                .content(content)
                .dateCreation(new Date())
                .read(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId);
    }

    @Override
    public void markAsRead(String id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
