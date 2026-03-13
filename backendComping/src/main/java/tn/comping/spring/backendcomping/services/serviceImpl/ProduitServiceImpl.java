package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitInter{

    private final ProduitRepository produitRepository;

         @Override
         public Produit addProduit(ProduitDTO produitDTO) {

        Produit produit = tn.comping.spring.backendcomping.mapper.ProduitMapper.toEntity(produitDTO);

        return produitRepository.save(produit);
    }

    @Override
    public List<Produit> getAllProduits() {

        return produitRepository.findAll();
    }

    @Override
    public String updateProduit(String id, ProduitDTO produitDTO) {

        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        tn.comping.spring.backendcomping.mapper.ProduitMapper.updateEntityFromDTO(produitDTO, produit);

        produitRepository.save(produit);

        return "Produit updated successfully";
    }

    @Override
    public String deleteProduit(String id) {

        produitRepository.deleteById(id);

        return "Produit deleted successfully";
    }
}
