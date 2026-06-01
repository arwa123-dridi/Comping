package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeLigneResponseDTO {

    private String produitId;
    private String nomProduit;
    private String imageUrl;

    private Double prixUnitaire;
    private Integer quantite;
    private Double sousTotal;
}