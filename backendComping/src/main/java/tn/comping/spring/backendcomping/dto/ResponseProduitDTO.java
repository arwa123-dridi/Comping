package tn.comping.spring.backendcomping.dto;

import tn.comping.spring.backendcomping.entities.categorieProduit;
import tn.comping.spring.backendcomping.entities.statutProduit;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseProduitDTO {
        private String id;
        private String nomProduit;
        private String descriptionProduit;
        private Double prixProduit;
        private categorieProduit categorieProduit;
        private statutProduit statut;
        // 🆕 STOCK MANAGEMENT
        private Integer quantiteStock; // current quantity in stock
        private Integer seuilAlerteStock; // low stock threshold
        private String imageUrl;

        private Double promoPrice;
        private LocalDateTime promoStart;
        private LocalDateTime promoEnd;

        // ⭐ FINAL PRICE CALCULATED
        private Double prixFinal;
        private Boolean hasPromotion;
}
