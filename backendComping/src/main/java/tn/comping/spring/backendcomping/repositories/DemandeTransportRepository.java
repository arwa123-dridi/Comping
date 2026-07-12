package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.DemandeTransport;
import tn.comping.spring.backendcomping.entities.StatutDemandeTransport;
import java.util.List;

@Repository
public interface DemandeTransportRepository extends MongoRepository<DemandeTransport, String> {
        List<DemandeTransport> findByUserId(String userId);
        List<DemandeTransport> findByUserIdOrderByDateCreationDesc(String userId);
        List<DemandeTransport> findByStatut(StatutDemandeTransport statut);
        List<DemandeTransport> findByCreneauLivraisonId(String creneauLivraisonId);
    }
