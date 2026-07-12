package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Notification;
import tn.comping.spring.backendcomping.entities.RefType;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByDestinataireUserIdOrDestinataireRoleOrderByDateCreationDesc(
            String destinataireUserId, String destinataireRole);

    long countByDestinataireUserIdAndLuFalse(String destinataireUserId);

    long countByDestinataireRoleAndLuFalse(String destinataireRole);

    List<Notification> findByRefTypeAndRefId(RefType refType, String refId);
}
