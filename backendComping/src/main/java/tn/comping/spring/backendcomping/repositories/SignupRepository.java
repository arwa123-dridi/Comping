package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.SignupEntity;

import java.util.List;
import java.util.Optional;

import tn.comping.spring.backendcomping.entities.Role;

@Repository
public interface SignupRepository extends MongoRepository<SignupEntity, String> {

    Optional<SignupEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    List<SignupEntity> findByRole(Role role);
    List<SignupEntity> findByRoleAndAddressContainingIgnoreCase(Role role, String city);

    @Query("{ 'email': { $regex: ?0, $options: 'i' } }")
    Optional<SignupEntity> findByEmailIgnoreCase(String email);
    
    Optional<SignupEntity> findByVerificationToken(String token);
}
