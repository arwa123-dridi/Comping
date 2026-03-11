package tn.comping.spring.backendcomping.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.SignupEntity;

@Repository
public interface SignupRepository extends MongoRepository<SignupEntity,String> {
    SignupEntity findByEmail(String email);
}