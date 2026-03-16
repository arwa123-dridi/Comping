package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.ReponseAvis;

import java.util.Optional;

@Repository
public interface ReponseAvisRepository extends
MongoRepository<ReponseAvis, String> {

    Optional<ReponseAvis> findByAvisId(String avisId);
    void deleteByAvisId(String avisId);
}