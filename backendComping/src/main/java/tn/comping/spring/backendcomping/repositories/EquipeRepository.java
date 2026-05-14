package tn.comping.spring.backendcomping.repositories;

import tn.comping.spring.backendcomping.entities.Equipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipeRepository extends MongoRepository<Equipe, String> {

    // ✅DBRef safe mapping
    List<Equipe> findByOrganisateur_Id(String organisateurId);

    // ✅ SAFE Mongo query
    @Query("{ $expr: { $lt: [ { $ifNull: [ { $size: '$membres' }, 0 ] }, '$nbMembresMax' ] } }")
    List<Equipe> findEquipesAvecPlace();
}