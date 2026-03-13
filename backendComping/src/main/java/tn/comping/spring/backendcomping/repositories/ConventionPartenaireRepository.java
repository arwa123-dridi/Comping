package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.ConventionPartenaire;

@Repository
public interface ConventionPartenaireRepository extends MongoRepository<ConventionPartenaire, String> {
}