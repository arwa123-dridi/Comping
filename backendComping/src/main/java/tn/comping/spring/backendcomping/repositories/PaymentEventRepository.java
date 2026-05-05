package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.PaymentEvent;

import java.util.List;

public interface PaymentEventRepository extends MongoRepository<PaymentEvent, String> {
    List<PaymentEvent> findByUserId(String userId);

    List<PaymentEvent> findByEventId(String eventId);
}
