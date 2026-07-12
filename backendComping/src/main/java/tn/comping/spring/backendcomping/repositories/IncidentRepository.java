package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Incident;
import tn.comping.spring.backendcomping.entities.StatutIncident;

import java.util.List;


@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {
    List<Incident> findByUserId(String userId);
    List<Incident> findByUserIdOrderByDateDeclarationDesc(String userId);
    List<Incident> findByStatut(StatutIncident statut);
    long countByUserId(String userId);
}
