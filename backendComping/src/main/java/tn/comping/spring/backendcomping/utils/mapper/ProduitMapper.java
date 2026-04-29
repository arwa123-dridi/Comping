package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.RequestProduitDTO;
import tn.comping.spring.backendcomping.dto.ResponseProduitDTO;
import tn.comping.spring.backendcomping.entities.Produit;

public class ProduitMapper {

    // =====================================================
    // CREATE PRODUCT (DTO → ENTITY)
    // =====================================================
    public static Produit toEntity(RequestProduitDTO dto) {
        return Produit.builder()
                .nomProduit(dto.getNomProduit())
                .descriptionProduit(dto.getDescriptionProduit())
                .prixProduit(dto.getPrixProduit())
                .categorieProduit(dto.getCategorieProduit())
                .imageUrl(dto.getImageUrl())

                // 🆕 STOCK MANAGEMENT
                .quantiteStock(dto.getQuantiteStock())
                .seuilAlerteStock(dto.getSeuilAlerteStock())

                // ⚠ statut will be calculated automatically later
                .build();
    }

    // =====================================================
    // ENTITY → RESPONSE DTO (for frontend)
    // =====================================================
    public static ResponseProduitDTO toResponseDTO(Produit produit) {
        return ResponseProduitDTO.builder()
                .id(produit.getId())
                .nomProduit(produit.getNomProduit())
                .descriptionProduit(produit.getDescriptionProduit())
                .prixProduit(produit.getPrixProduit())
                .categorieProduit(produit.getCategorieProduit())
                .statut(produit.getStatut())
                .imageUrl(produit.getImageUrl())

                // 🆕 STOCK MANAGEMENT
                .quantiteStock(produit.getQuantiteStock())
                .seuilAlerteStock(produit.getSeuilAlerteStock())

                .build();
    }

    // =====================================================
    // UPDATE PRODUCT (DTO → EXISTING ENTITY)
    // =====================================================
    public static void updateEntityFromDTO(RequestProduitDTO dto, Produit produit) {

        // Basic info
        produit.setNomProduit(dto.getNomProduit());
        produit.setDescriptionProduit(dto.getDescriptionProduit());
        produit.setPrixProduit(dto.getPrixProduit());
        produit.setCategorieProduit(dto.getCategorieProduit());
        produit.setImageUrl(dto.getImageUrl());

        // 🆕 Admin can update stock & threshold
        produit.setQuantiteStock(dto.getQuantiteStock());
        produit.setSeuilAlerteStock(dto.getSeuilAlerteStock());

        // ❌ DO NOT update statut here
        // It will be recalculated automatically by StockService
    }
}