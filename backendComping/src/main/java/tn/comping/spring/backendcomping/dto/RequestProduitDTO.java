package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.categorieProduit;
import tn.comping.spring.backendcomping.entities.statutProduit;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestProduitDTO {

    private String nomProduit;
    private String descriptionProduit;
    private Double prixProduit;

    private categorieProduit categorieProduit;

    // STOCK MANAGEMENT
    private Integer quantiteStock;
    private Integer seuilAlerteStock;

    private statutProduit statut;

    private String imageUrl;

    // PROMOTION (from theirs branch - kept and integrated)
    private Double promoPrice;
    private LocalDateTime promoStart;
    private LocalDateTime promoEnd;
}