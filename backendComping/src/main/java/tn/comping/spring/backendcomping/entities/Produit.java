package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "produit")
public class Produit {

    @Id
    private String id;

    private String nomProduit;
    private String descriptionProduit;
    private Double prixProduit;

    private categorieProduit categorieProduit;
    private statutProduit statut;

    // STOCK MANAGEMENT
    private Integer quantiteStock;
    private Integer seuilAlerteStock;

    private String imageUrl;

    // PROMOTION
    private Double promoPrice;
    private LocalDateTime promoStart;
    private LocalDateTime promoEnd;
}