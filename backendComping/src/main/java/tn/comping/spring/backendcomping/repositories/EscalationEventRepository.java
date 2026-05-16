package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.EscalationEvent;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EscalationEventRepository extends MongoRepository<EscalationEvent, String> {
    @org.springframework.data.mongodb.repository.Query("{ 'incidentOrAlertId' : ?0 }")
    List<EscalationEvent> findByIncidentOrAlertId(String incidentOrAlertId);
    List<EscalationEvent> findBySourceType(String sourceType);
    List<EscalationEvent> findByEscalationTimeAfter(LocalDateTime time);
    List<EscalationEvent> findByNotificationAcknowledgedIsFalse();
}
