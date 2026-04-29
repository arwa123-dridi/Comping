package tn.comping.spring.backendcomping.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Produit;

@Repository
public interface ProduitRepository extends MongoRepository<Produit, String> {

List<Produit> findByNomProduitContainingIgnoreCase(String nomProduit);

}