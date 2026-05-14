package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.comping.spring.backendcomping.entities.CommandeProduct;
import tn.comping.spring.backendcomping.entities.Panier;
import tn.comping.spring.backendcomping.entities.PanierStatut;

import java.util.List;

public interface CommandeRepository extends MongoRepository<CommandeProduct, String> {

    List<CommandeProduct> findByUserId(String userId);

}