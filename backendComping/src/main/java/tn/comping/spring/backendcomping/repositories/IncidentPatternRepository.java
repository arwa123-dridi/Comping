package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.IncidentPattern;
import java.util.List;

@Repository
public interface IncidentPatternRepository extends MongoRepository<IncidentPattern, String> {
    List<IncidentPattern> findByTriggerIncidentTypesContaining(String incidentType);
    List<IncidentPattern> findByEnabledIsTrue();
    List<IncidentPattern> findByPredictedIncidentType(String type);
}
