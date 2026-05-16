package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;
import tn.comping.spring.backendcomping.repositories.ProduitRepository;
import tn.comping.spring.backendcomping.utils.mapper.ProduitMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitInter {

    private final ProduitRepository produitRepository;

    @Override
    public ResponseProduitDTO addProduit(RequestProduitDTO produitDTO) {
        Produit produit = ProduitMapper.toEntity(produitDTO); // maps RequestProduitDTO -> Produit
        Produit savedProduit = produitRepository.save(produit);
        return ProduitMapper.toResponseDTO(savedProduit);    // maps Produit -> ResponseProduitDTO
    }

    @Override
    public List<ResponseProduitDTO> getAllProduits() {
        return produitRepository.findAll()
                .stream()
                .map(ProduitMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseProduitDTO updateProduit(String id, RequestProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit not found"));

        ProduitMapper.updateEntityFromDTO(produitDTO, produit); // updates Produit from RequestProduitDTO
        Produit updatedProduit = produitRepository.save(produit);
        return ProduitMapper.toResponseDTO(updatedProduit);
    }

    @Override
    public String deleteProduit(String id) {
        produitRepository.deleteById(id);
        return "Produit deleted successfully";
    }
}