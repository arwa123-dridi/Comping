package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import tn.comping.spring.backendcomping.entities.CommandeProduct;
import tn.comping.spring.backendcomping.entities.Panier;
import tn.comping.spring.backendcomping.entities.PanierStatut;
import tn.comping.spring.backendcomping.entities.StatutCommande;

import java.util.List;

public interface CommandeRepository extends MongoRepository<CommandeProduct, String> {

    List<CommandeProduct> findByUserId(String userId);

    List<CommandeProduct> findByLivreurId(String livreurId);

    List<CommandeProduct> findByStatutCommande(StatutCommande statutCommande);

    List<CommandeProduct> findByLivreurIdAndStatutCommandeNot(
            String livreurId,
            StatutCommande statutCommande);

}