package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Participation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends MongoRepository<Participation, String> {

    // Utilise l'ID de la sortie depuis la référence
    @Query("{ 'sortie.$id' : ?0 }")
    List<Participation> findBySortieId(String sortieId);

    //  Utilise l'ID de l'utilisateur depuis la référence
    @Query("{ 'utilisateur.$id' : ?0 }")
    List<Participation> findByUtilisateurId(String utilisateurId);

    // = Recherche par les deux IDs
    @Query("{ 'utilisateur.$id' : ?0, 'sortie.$id' : ?1 }")
    Optional<Participation> findByUtilisateurIdAndSortieId(String utilisateurId, String sortieId);

    //  Suppression par ID de sortie
    @Query(value = "{ 'sortie.$id' : ?0 }", delete = true)
    void deleteBySortieId(String sortieId);

    // Comptage par ID de sortie
    long countBySortieId(String sortieId);
}