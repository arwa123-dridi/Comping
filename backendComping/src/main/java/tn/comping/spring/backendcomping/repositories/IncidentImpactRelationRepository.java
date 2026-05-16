package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.IncidentImpactRelation;
import java.util.List;

@Repository
public interface IncidentImpactRelationRepository extends MongoRepository<IncidentImpactRelation, String> {
    List<IncidentImpactRelation> findByCauseIncidentId(String incidentId);
    List<IncidentImpactRelation> findByAffectedIncidentId(String incidentId);
    List<IncidentImpactRelation> findByRelationshipType(String type);
}
