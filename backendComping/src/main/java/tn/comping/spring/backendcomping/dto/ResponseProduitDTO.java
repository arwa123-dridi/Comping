package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseProduitDTO {
      
        private String nomProduit;
        private String descriptionProduit;
        private Double prixProduit;
        private Integer categorieProduit;
        private String typeProduit;
        private String statut;

}
