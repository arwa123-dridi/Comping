package tn.comping.spring.backendcomping.entities;

import lombok.*;
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
        private Integer categorieProduit;
        private String typeProduit;
        private String statut;

    }

