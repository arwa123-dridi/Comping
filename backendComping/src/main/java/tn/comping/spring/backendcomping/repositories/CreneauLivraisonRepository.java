package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.CreneauLivraison;


@Repository
public interface CreneauLivraisonRepository extends MongoRepository<CreneauLivraison, String> {
}
