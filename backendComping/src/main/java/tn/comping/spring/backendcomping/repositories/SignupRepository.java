package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.SignupEntity;

import java.util.Optional;

@Repository
public interface SignupRepository extends MongoRepository<SignupEntity, String> {

    Optional<SignupEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("{ 'email': { $regex: ?0, $options: 'i' } }")
    Optional<SignupEntity> findByEmailIgnoreCase(String email);
}