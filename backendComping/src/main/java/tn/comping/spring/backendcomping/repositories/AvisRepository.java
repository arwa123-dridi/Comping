package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Avis;
import java.util.List;

@Repository
public interface AvisRepository extends MongoRepository<Avis, String> {
    List<Avis> findBySiteCampingId(String siteCampingId);
    List<Avis> findByUtilisateurId(String utilisateurId);
    List<Avis> findBySiteCampingIdAndStatutModeration(String siteCampingId, String statut);
}