package tn.comping.spring.backendcomping.repositories;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Event;

@Repository
public interface EventRepository extends MongoRepository<Event,String> {
}
