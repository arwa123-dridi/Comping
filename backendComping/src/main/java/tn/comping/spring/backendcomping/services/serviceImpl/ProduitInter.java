package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.ProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

import java.util.List;

public interface ProduitInter {
    Produit addProduit(ProduitDTO produitDTO);

    List<Produit> getAllProduits();

    String updateProduit(String id, ProduitDTO produitDTO);

    String deleteProduit(String id);
}
