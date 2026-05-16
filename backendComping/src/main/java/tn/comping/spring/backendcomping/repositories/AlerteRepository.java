package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Alerte;
import java.util.List;

@Repository
public interface AlerteRepository extends MongoRepository<Alerte, String> {
    List<Alerte> findBySiteCampingId(String siteCampingId);
    List<Alerte> findByStatut(String statut);
}