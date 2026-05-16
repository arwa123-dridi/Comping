package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.Activity;

public interface ActivityRepository extends MongoRepository<Activity, String> {
}
