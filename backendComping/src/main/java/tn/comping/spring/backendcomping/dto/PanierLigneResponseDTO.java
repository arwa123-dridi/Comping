package tn.comping.spring.backendcomping.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PanierLigneResponseDTO {

    private String produitId;

    private String nomProduit;

    private Double prixUnitaire;

    private Integer quantite;

    private String imageUrl;

    private Double sousTotal;
}