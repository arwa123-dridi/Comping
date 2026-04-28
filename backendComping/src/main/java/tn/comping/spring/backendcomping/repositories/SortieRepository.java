package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Participation;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.StatutSortie;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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



    // Utilise l'ID de la sortie depuis la référence
    @Query("{ 'sortie.$id' : ?0 }")
    List<Participation> findBySortieId(String sortieId);

    // = Recherche par les deux IDs
    @Query("{ 'utilisateur.$id' : ?0, 'sortie.$id' : ?1 }")
    Optional<Participation> findByUtilisateurIdAndSortieId(String utilisateurId, String sortieId);

    //  Suppression par ID de sortie
    @Query(value = "{ 'sortie.$id' : ?0 }", delete = true)
    void deleteBySortieId(String sortieId);

    // Comptage par ID de sortie
    long countById(String id);
    
    // Compter les participations d'un user (pour seuil historique)
    @Query(value = "{ 'utilisateur.$id' : ?0 }", count = true)
    long countByUtilisateurId(String utilisateurId);

    // Toutes les participations triées par date DESC
    @Query("{ 'utilisateur.$id' : ?0 }")
    List<Participation> findByUtilisateurIdOrderByDateInscriptionDesc(String utilisateurId);


    // Vérifier si un utilisateur est déjà inscrit à une sortie
    @Query(value = "{ 'utilisateur.$id' : ?0, 'sortie.$id' : ?1 }", exists = true)
    boolean existsByUtilisateurIdAndSortieId(String utilisateurId, String sortieId);

    // Récupérer toutes les participations d’un user (pour l’historique)
    @Query("{ 'utilisateur.$id' : ?0 }")
    List<Participation> findByUtilisateurId(String utilisateurId);


}