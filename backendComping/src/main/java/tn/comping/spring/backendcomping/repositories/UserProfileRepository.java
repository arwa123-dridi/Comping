package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.UserProfile;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByUtilisateurId(String utilisateurId);
    void deleteByUtilisateurId(String utilisateurId);
}
