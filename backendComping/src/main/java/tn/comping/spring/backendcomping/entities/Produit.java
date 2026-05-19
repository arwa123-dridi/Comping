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
@Document(collection = "produit")
public class Produit {

    @Id
    private String id;

    private String nomProduit;
    private String descriptionProduit;
    private Double prixProduit;
    private categorieProduit categorieProduit;
    private statutProduit statut;
    private Integer quantiteStock;
    private Integer seuilAlerteStock;
    private String imageUrl;
    private Double promoPrice;
    private LocalDateTime promoStart;
    private LocalDateTime promoEnd;
}
