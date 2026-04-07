package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Interaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends MongoRepository<Interaction, String> {
    List<Interaction> findByCibleTypeAndCibleIdOrderByIdDesc(String cibleType, String cibleId);
    Optional<Interaction> findByAuteurIdAndCibleTypeAndCibleIdAndType(String auteurId, String cibleType, String cibleId, String type);
    long countByCibleTypeAndCibleIdAndType(String cibleType, String cibleId, String type); // likes count
    long countByCibleTypeAndCibleId(String cibleType, String cibleId); // total interactions
}
