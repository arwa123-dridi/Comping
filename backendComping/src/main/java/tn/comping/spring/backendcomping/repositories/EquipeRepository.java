package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'entité Equipe.
 * Hérite de MongoRepository<Equipe, String> → fournit les CRUD de base.
 */
@Repository
public interface EquipeRepository extends MongoRepository<Equipe, String> {

    /**
     * Recherche les équipes dont l'organisateur possède un identifiant donné.
     *
     * MongoDB stocke les références @DBRef sous forme d'objet avec $id.
     * Spring Data MongoDB convertit automatiquement le champ 'organisateur.id'
     * en la requête MongoDB : { "organisateur.$id" : ?0 }.
     *
     * @param organisateurId l'identifiant de l'organisateur
     * @return liste des équipes dont l'organisateur correspond
     */
    // ✅ DBRef safe mapping
    List<Equipe> findByOrganisateur_Id(String organisateurId);

    /**
     * Recherche les équipes qui ont encore de la place (membres < nbMembresMax).
     *
     * Requête MongoDB personnalisée, nécessaire car le champ 'membres' est une
     * liste référencée (@DBRef). Spring Data ne peut pas générer automatiquement
     * cette condition sur la taille d'une liste DBRef.
     *
     * Détails de la requête :
     *   - { $size: '$membres' } → taille de la liste membres (échoue si membres est null)
     *   - { $ifNull: [ { $size: '$membres' }, 0 ] } → retourne 0 si membres est null
     *   - { $lt: [ ... , '$nbMembresMax' ] } → compare cette taille avec nbMembresMax
     *   - $expr permet d'utiliser des expressions dans la condition de filtrage.
     *
     * @return liste des équipes avec places disponibles
     */
    // ✅ SAFE Mongo query
    @Query("{ $expr: { $lt: [ { $ifNull: [ { $size: '$membres' }, 0 ] }, '$nbMembresMax' ] } }")
    List<Equipe> findEquipesAvecPlace();
}