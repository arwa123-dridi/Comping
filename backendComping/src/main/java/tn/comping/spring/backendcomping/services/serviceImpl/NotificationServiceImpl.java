package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.NotificationResponse;
import tn.comping.spring.backendcomping.entities.Notification;
import tn.comping.spring.backendcomping.entities.NotificationType;
import tn.comping.spring.backendcomping.entities.RefType;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.repositories.NotificationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.NotificationMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SignupRepository signupRepository;

    @Override
    public void notifyRole(String destinataireRole, NotificationType type, String titre, String message,
                            RefType refType, String refId, String lien) {
        notificationRepository.save(Notification.builder()
                .destinataireRole(destinataireRole)
                .type(type)
                .titre(titre)
                .message(message)
                .lu(false)
                .dateCreation(new Date())
                .refType(refType)
                .refId(refId)
                .lien(lien)
                .build());
    }

    @Override
    public void notifyUser(String destinataireUserId, NotificationType type, String titre, String message,
                            RefType refType, String refId, String lien) {
        notificationRepository.save(Notification.builder()
                .destinataireUserId(destinataireUserId)
                .type(type)
                .titre(titre)
                .message(message)
                .lu(false)
                .dateCreation(new Date())
                .refType(refType)
                .refId(refId)
                .lien(lien)
                .build());
    }

    @Override
    public List<NotificationResponse> getForCurrentUser(String email) {
        SignupEntity user = currentUser(email);
        return notificationRepository
                .findByDestinataireUserIdOrDestinataireRoleOrderByDateCreationDesc(user.getId(), user.getRole().name())
                .stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long unreadCountForCurrentUser(String email) {
        SignupEntity user = currentUser(email);
        return notificationRepository.countByDestinataireUserIdAndLuFalse(user.getId())
                + notificationRepository.countByDestinataireRoleAndLuFalse(user.getRole().name());
    }

    @Override
    public void markRead(String id, String email) {
        SignupEntity user = currentUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification non trouvee"));

        boolean isOwner = user.getId().equals(notification.getDestinataireUserId());
        boolean isRoleTarget = notification.getDestinataireUserId() == null
                && user.getRole().name().equals(notification.getDestinataireRole());
        if (!isOwner && !isRoleTarget) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'etes pas autorise a modifier cette notification");
        }
        notification.setLu(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllReadForCurrentUser(String email) {
        SignupEntity user = currentUser(email);
        List<Notification> notifications = notificationRepository
                .findByDestinataireUserIdOrDestinataireRoleOrderByDateCreationDesc(user.getId(), user.getRole().name());
        notifications.forEach(n -> n.setLu(true));
        notificationRepository.saveAll(notifications);
    }

    private SignupEntity currentUser(String email) {
        return signupRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouve"));
    }
}
