package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.CarteFidelite;

import java.util.Optional;

public interface CarteFideliteRepository extends MongoRepository<CarteFidelite, String> {
    Optional<CarteFidelite> findByClientId(String clientId);
}
