package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Interaction;

import java.util.List;

public interface InteractionRepository extends MongoRepository<Interaction, String> {
    List<Interaction> findByAuteurIdAndType(String auteurId, String type);
    List<Interaction> findByCibleIdInAndTypeAndAuteurIdNot(List<String> cibleIds, String type, String auteurId);
    List<Interaction> findByAuteurIdInAndTypeAndCibleIdNotIn(List<String> auteurIds, String type, List<String> excludedCibleIds);
}
