package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "commandes")
public class CommandeProduct {

    @Id
    private String id;

    private String userId;
    private String livreurId;
    private List<CommandeLigne> lignes;

    // pricing
    private Double totalProduits;
    private Double fraisLivraison;
    private Double totalCommande;

    // choices
    private ModePaiement modePaiement;
    private ModeLivraison modeLivraison;

    private StatutCommande statutCommande;

    private AdresseLivraison adresseLivraison;

    private LocalDateTime dateCommande;
}
