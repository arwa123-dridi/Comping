package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.Difficulte;      // ✅ Import correct (dans entities)
import tn.comping.spring.backendcomping.entities.StatutSortie;    // ✅ Import correct (dans entities)
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface SortieRepository extends MongoRepository<Sortie, String> {

    // ✅ Recherche par ID de l'organisateur (référence MongoDB)
    @Query("{ 'organisateur.$id' : ?0 }")
    List<Sortie> findByOrganisateurId(String organisateurId);

    // ✅ Recherche par ID d'équipe (référence MongoDB)
    @Query("{ 'equipe.$id' : ?0 }")
    List<Sortie> findByEquipeId(String equipeId);
    //  Recherche par région
    List<Sortie> findByRegion(String region);

    //  Recherche par statut
    List<Sortie> findByStatut(StatutSortie statut);

    //  Recherche entre deux dates
    @Query("{ 'dateDebut' : { $gte: ?0, $lte: ?1 } }")
    List<Sortie> findBetweenDates(LocalDateTime debut, LocalDateTime fin);

    // Sorties futures (avec LocalDateTime)
    @Query("{ 'dateDebut' : { $gt : ?0 } }")
    List<Sortie> findByDateDebutAfter(LocalDateTime date);

}