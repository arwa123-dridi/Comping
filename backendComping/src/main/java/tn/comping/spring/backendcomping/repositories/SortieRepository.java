package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.enums.Difficulte;
import tn.comping.spring.backendcomping.enums.StatutSortie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SortieRepository extends MongoRepository<Sortie, String> {
    List<Sortie> findByOrganisateurId(String organisateurId);
    List<Sortie> findByEquipeId(String equipeId);
    List<Sortie> findByRegion(String region);
    List<Sortie> findByStatut(StatutSortie statut);

    @Query("{ 'dateDebut' : { $gte: ?0, $lte: ?1 } }")
    List<Sortie> findBetweenDates(LocalDateTime debut, LocalDateTime fin);
}