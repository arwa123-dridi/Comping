package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.PaymentEvent;
import tn.comping.spring.backendcomping.entities.PaymentEventStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentEventRepository extends MongoRepository<PaymentEvent, String> {
    List<PaymentEvent> findByUserId(String userId);
    Optional<PaymentEvent> findFirstByEventIdAndUserIdAndStatus(
            String eventId, String userId, PaymentEventStatus status
    );
    List<PaymentEvent> findByEventId(String eventId);
    Optional<PaymentEvent> findFirstByEventIdAndUserId(String eventId, String userId);
    boolean existsByEventIdAndUserIdAndStatus(
            String eventId, String userId, PaymentEventStatus status
    );
    boolean existsByEventIdAndUserId(String eventId, String userId);
}
