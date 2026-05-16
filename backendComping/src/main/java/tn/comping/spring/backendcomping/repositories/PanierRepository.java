package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Panier;
import tn.comping.spring.backendcomping.entities.PanierStatut;

import java.util.Optional;

public interface PanierRepository extends MongoRepository<Panier, String> {

    Optional<Panier> findByUserIdAndStatut(String userId, PanierStatut statut);

    Optional<Panier> findByUserId(String userId);

    long countByUserId(String userId);
}