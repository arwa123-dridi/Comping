package tn.comping.spring.backendcomping.entities;

import lombok.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Data
// MongoDB collection name

@Document(collection = "produit")
public class Produit {

    @Id
    private String id; // MongoDB ObjectId, String is typical

    private String nomProduit;
    private String descriptionProduit;
    private Double prixProduit;
    private categorieProduit categorieProduit;
    private statutProduit statut;
    // 🆕 STOCK MANAGEMENT
    private Integer quantiteStock; // current quantity in stock
    private Integer seuilAlerteStock; // low stock threshold
    private String imageUrl;
<<<<<<< HEAD
    private Double promoPrice;

    private LocalDateTime promoStart;
    private LocalDateTime promoEnd;
    
=======

>>>>>>> origin/ahmed
}
