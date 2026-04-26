package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Panier;

import java.util.Optional;

public interface PanierRepository extends MongoRepository<Panier, String> {

    // Get active cart of a user
    Optional<Panier> findByUserIdAndStatut(String userId, String statut);

    // Get cart by user (if you want without status filter)
    Optional<Panier> findByUserId(String userId);
}