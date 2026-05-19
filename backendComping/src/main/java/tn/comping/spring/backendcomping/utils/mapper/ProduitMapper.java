package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

public class ProduitMapper {

    public static Produit toEntity(RequestProduitDTO dto) {
        return Produit.builder()
                .nomProduit(dto.getNomProduit())
                .descriptionProduit(dto.getDescriptionProduit())
                .prixProduit(dto.getPrixProduit())
                .categorieProduit(dto.getCategorieProduit())
                .imageUrl(dto.getImageUrl())
                .quantiteStock(dto.getQuantiteStock())
                .seuilAlerteStock(dto.getSeuilAlerteStock())
                .promoPrice(dto.getPromoPrice())
                .promoStart(dto.getPromoStart())
                .promoEnd(dto.getPromoEnd())
                .build();
    }

    public static ResponseProduitDTO toResponseDTO(
            Produit produit,
            Double finalPrice,
            Boolean promoActive) {
        return ResponseProduitDTO.builder()
                .id(produit.getId())
                .nomProduit(produit.getNomProduit())
                .descriptionProduit(produit.getDescriptionProduit())
                .prixProduit(produit.getPrixProduit())
                .categorieProduit(produit.getCategorieProduit())
                .statut(produit.getStatut())
                .imageUrl(produit.getImageUrl())
                .quantiteStock(produit.getQuantiteStock())
                .seuilAlerteStock(produit.getSeuilAlerteStock())
                .promoPrice(produit.getPromoPrice())
                .promoStart(produit.getPromoStart())
                .promoEnd(produit.getPromoEnd())
                .prixFinal(finalPrice)
                .hasPromotion(promoActive)
                .build();
    }

    public static void updateEntityFromDTO(RequestProduitDTO dto, Produit produit) {
        produit.setNomProduit(dto.getNomProduit());
        produit.setDescriptionProduit(dto.getDescriptionProduit());
        produit.setPrixProduit(dto.getPrixProduit());
        produit.setCategorieProduit(dto.getCategorieProduit());
        produit.setImageUrl(dto.getImageUrl());
        produit.setQuantiteStock(dto.getQuantiteStock());
        produit.setSeuilAlerteStock(dto.getSeuilAlerteStock());
        produit.setPromoPrice(dto.getPromoPrice());
        produit.setPromoStart(dto.getPromoStart());
        produit.setPromoEnd(dto.getPromoEnd());
    }
}
