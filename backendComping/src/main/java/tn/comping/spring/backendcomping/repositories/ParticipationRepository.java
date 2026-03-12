package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Participation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends MongoRepository<Participation, String> {
    List<Participation> findBySortieId(String sortieId);
    List<Participation> findByUtilisateurId(String utilisateurId);
    Optional<Participation> findByUtilisateurIdAndSortieId(String utilisateurId, String sortieId);

    @Query(value = "{ 'sortieId': ?0 }", delete = true)
    void deleteBySortieId(String sortieId);

    long countBySortieId(String sortieId);
}