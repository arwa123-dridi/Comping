package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Role;
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProduitDTO {
        private String nomProduit;
        private String descriptionProduit;
        private Double prixProduit;
        private Integer  stockProduit;
        private String typeProduit;

}
