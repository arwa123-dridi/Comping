package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

public class ProduitMapper {

    // Convert DTO to entity
    public static Produit toEntity(RequestProduitDTO dto) {
        return Produit.builder()
                .nomProduit(dto.getNomProduit())
                .descriptionProduit(dto.getDescriptionProduit())
                .prixProduit(dto.getPrixProduit())
                .categorieProduit(dto.getCategorieProduit()) // now enum
                .typeProduit(dto.getTypeProduit())
                .statut(dto.getStatut()) // now enum
                .build();
    }

    // Convert entity to Response DTO
    public static ResponseProduitDTO toResponseDTO(Produit produit) {
        return ResponseProduitDTO.builder()
                .id(produit.getId())
                .nomProduit(produit.getNomProduit())
                .descriptionProduit(produit.getDescriptionProduit())
                .prixProduit(produit.getPrixProduit())
                .categorieProduit(produit.getCategorieProduit()) // enum
                .typeProduit(produit.getTypeProduit())
                .statut(produit.getStatut()) // enum
                .imageUrl(produit.getImageUrl())
                .build();
    }

    // Update existing entity from DTO
    public static void updateEntityFromDTO(RequestProduitDTO dto, Produit produit) {
        produit.setNomProduit(dto.getNomProduit());
        produit.setDescriptionProduit(dto.getDescriptionProduit());
        produit.setPrixProduit(dto.getPrixProduit());
        produit.setCategorieProduit(dto.getCategorieProduit()); // enum
        produit.setTypeProduit(dto.getTypeProduit());
        produit.setStatut(dto.getStatut()); // enum
    }
}