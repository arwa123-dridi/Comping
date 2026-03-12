package tn.comping.spring.backendcomping.mapper;

import tn.comping.spring.backendcomping.dto.ProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

public class ProduitMapper {

    public static Produit toEntity(ProduitDTO dto) {

        return Produit.builder()
                .nomProduit(dto.getNomProduit())
                .descriptionProduit(dto.getDescriptionProduit())
                .prixProduit(dto.getPrixProduit())
                .stockProduit(dto.getStockProduit())
                .typeProduit(dto.getTypeProduit())
                .build();
    }

    public static ProduitDTO toDTO(Produit produit) {

        return ProduitDTO.builder()
                .nomProduit(produit.getNomProduit())
                .descriptionProduit(produit.getDescriptionProduit())
                .prixProduit(produit.getPrixProduit())
                .stockProduit(produit.getStockProduit())
                .typeProduit(produit.getTypeProduit())
                .build();
    }

    public static void updateEntityFromDTO(ProduitDTO dto, Produit produit) {
        produit.setNomProduit(dto.getNomProduit());
        produit.setDescriptionProduit(dto.getDescriptionProduit());
        produit.setPrixProduit(dto.getPrixProduit());
        produit.setStockProduit(dto.getStockProduit());
        produit.setTypeProduit(dto.getTypeProduit());
    }
}