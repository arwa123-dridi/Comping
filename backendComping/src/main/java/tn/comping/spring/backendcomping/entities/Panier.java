package tn.comping.spring.backendcomping.entities;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Document(collection = "panier")
public class Panier {
     @Id
    private String id;

    // Reference to SignupEntity.id
    private String userId;

    private List<PanierLigne> lignes;

    private Double totalPrice;

    // ACTIVE, CHECKOUT, ORDERED
    private PanierStatut statut;
}
