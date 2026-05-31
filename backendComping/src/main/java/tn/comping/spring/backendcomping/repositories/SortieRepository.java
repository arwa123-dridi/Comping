package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.StatutSortie;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SortieRepository extends MongoRepository<Sortie, String> {

    // ── Recherche par organisateur ────────────────────────
    @Query("{ 'organisateur.$id' : ?0 }")
    List<Sortie> findByOrganisateurId(String organisateurId);

    // ── Recherche par équipe ──────────────────────────────
    @Query("{ 'equipe.$id' : ?0 }")
    List<Sortie> findByEquipeId(String equipeId);

    // ── Recherche par région ──────────────────────────────
    List<Sortie> findByRegion(String region);

    // ── Recherche par statut ──────────────────────────────
    List<Sortie> findByStatut(StatutSortie statut);

    // ── Sorties entre deux dates (utilisé par PlanningService) ──
    @Query("{ 'dateDebut' : { $gte: ?0, $lte: ?1 } }")
    List<Sortie> findBetweenDates(LocalDateTime debut, LocalDateTime fin);

    // ── Sorties futures (utilisé par RecommandationService) ──
    @Query("{ 'dateDebut' : { $gt : ?0 } }")
    List<Sortie> findByDateDebutAfter(LocalDateTime date);


}