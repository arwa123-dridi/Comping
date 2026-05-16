package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Urgence;
import java.util.Date;
import java.util.List;

@Repository
public interface UrgenceRepository extends MongoRepository<Urgence, String> {
    List<Urgence> findBySiteCampingId(String siteCampingId);
    List<Urgence> findByStatut(String statut);
    List<Urgence> findBySiteCampingIdAndStatut(String siteCampingId, String statut);
    List<Urgence> findByUserId(String userId);
    List<Urgence> findByAssigneId(String assigneId);
    List<Urgence> findByNiveauUrgence(String niveauUrgence);
    List<Urgence> findByPriorite(String priorite);
    List<Urgence> findByCategorie(String categorie);
    List<Urgence> findByDateCreationBetween(Date startDate, Date endDate);
    List<Urgence> findByStatutAndNiveauUrgence(String statut, String niveauUrgence);
    List<Urgence> findByNumberOfEscalationsGreaterThan(Integer threshold);
}
