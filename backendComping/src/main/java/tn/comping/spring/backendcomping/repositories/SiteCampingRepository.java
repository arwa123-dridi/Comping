package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.SiteCamping;
import java.util.List;

@Repository
public interface SiteCampingRepository extends MongoRepository<SiteCamping, String> {
    List<SiteCamping> findByDisponibleTrue();
    List<SiteCamping> findByLocalisation(String localisation);
    List<SiteCamping> findByTarifsBetween(double min, double max);
    List<SiteCamping> findByProprietaireId(String proprietaireId);
}