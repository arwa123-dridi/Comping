package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Activity;

import java.util.List;

public interface ActivityRepository extends MongoRepository<Activity, String> {
    // Recherche par type
    List<Activity> findByType(String type);
    List<Activity> findByNiveauDifficulte(String niveau);
    List<Activity> findByTrancheAge(String trancheAge);
    List<Activity> findBySaison(String saison);
    List<Activity> findByTypeAndNiveauDifficulte(String type, String niveau);
    List<Activity> findByTypeAndTrancheAge(String type, String trancheAge);



}
