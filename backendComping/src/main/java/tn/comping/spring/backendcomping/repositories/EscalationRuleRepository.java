package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.EscalationRule;
import java.util.List;

@Repository
public interface EscalationRuleRepository extends MongoRepository<EscalationRule, String> {
    List<EscalationRule> findByEnabledIsTrue();
    List<EscalationRule> findBySourceType(String sourceType);
    List<EscalationRule> findByTriggerPriority(String priority);
}
