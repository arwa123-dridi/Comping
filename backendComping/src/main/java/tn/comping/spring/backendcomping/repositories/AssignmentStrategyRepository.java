package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.AssignmentStrategy;
import java.util.List;

@Repository
public interface AssignmentStrategyRepository extends MongoRepository<AssignmentStrategy, String> {
    List<AssignmentStrategy> findByEnabledIsTrue();
    List<AssignmentStrategy> findByTypeOrderByPriorityDesc(AssignmentStrategy.AssignmentType type);
}
