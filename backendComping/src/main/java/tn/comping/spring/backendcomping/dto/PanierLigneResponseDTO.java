package tn.comping.spring.backendcomping.dto;

import java.time.LocalDateTime;

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

     private Boolean promoActive;

    private LocalDateTime promoEnd;
}