package tn.comping.spring.backendcomping.entities;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PanierLigne {
    private String produitId;

    private String nomProduit;

    private Double prixUnitaire;

    private Integer quantite;

    private String imageUrl;

    private Double sousTotal;
}
