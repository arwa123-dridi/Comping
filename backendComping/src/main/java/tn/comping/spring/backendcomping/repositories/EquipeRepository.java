package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipeRepository extends MongoRepository<Equipe, String> {

    // ⚠️ MODIFIÉ : Recherche par ID de l'organisateur
    @Query("{ 'organisateur.$id' : ?0 }")
    List<Equipe> findByOrganisateurId(String organisateurId);

    // ⚠️ MODIFIÉ : Équipes avec places disponibles
    @Query("{ $expr: { $lt: [ { $size: '$membres' }, '$nbMembresMax' ] } }")
    List<Equipe> findEquipesAvecPlace();
}