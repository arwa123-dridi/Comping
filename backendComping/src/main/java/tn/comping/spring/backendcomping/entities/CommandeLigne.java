package tn.comping.spring.backendcomping.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeLigne {

    private String produitId;
    private String nomProduit;
    private String imageUrl;

    private Double prixUnitaire;
    private Integer quantite;
    private Double sousTotal;
}