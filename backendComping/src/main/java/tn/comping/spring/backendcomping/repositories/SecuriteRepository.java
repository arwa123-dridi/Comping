package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Securite;
import java.util.Date;
import java.util.List;

@Repository
public interface SecuriteRepository extends MongoRepository<Securite, String> {
    List<Securite> findBySiteCampingId(String siteCampingId);
    List<Securite> findByStatut(String statut);
    List<Securite> findBySiteCampingIdAndStatut(String siteCampingId, String statut);
    List<Securite> findByResponsableId(String responsableId);
    List<Securite> findByNiveauSecurite(String niveauSecurite);
    List<Securite> findByRiskLevel(String riskLevel);
    List<Securite> findByTypeMesure(String typeMesure);
    List<Securite> findByMonitoringType(String monitoringType);
    List<Securite> findByDateCreationBetween(Date startDate, Date endDate);
    List<Securite> findByTeamMemberIdsContaining(String memberId);
    List<Securite> findBySecurityScoreLessThan(Integer threshold);
    List<Securite> findByRiskScoreGreaterThan(Integer threshold);
    long countBySiteCampingIdAndStatut(String siteCampingId, String statut);
}
