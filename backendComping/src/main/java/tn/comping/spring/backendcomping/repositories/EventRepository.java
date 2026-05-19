package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Event;
import tn.comping.spring.backendcomping.entities.StatutEvent;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByOrganisateurId(String organisateurId);
    List<Event> findByStatut(StatutEvent statut);
    long countByStatut(StatutEvent statut);
    List<Event> findTop5ByOrderByDateDebutDesc();
}
