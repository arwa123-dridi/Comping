package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Abonnement;

import java.util.List;

@Repository
public interface AbonnementRepository extends MongoRepository<Abonnement, String> {
    List<Abonnement> findBySuiveurId(String suiveurId);
    List<Abonnement> findBySuiviId(String suiviId);
    long countBySuiveurId(String suiveurId);
    long countBySuiviId(String suiviId);
    boolean existsBySuiveurIdAndSuiviId(String suiveurId, String suiviId);
}
