package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.AdresseLivraison;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeResponseDTO {

    private String id;
    private String userId;

    // ✔ ONLY ONE STRUCTURE
    private AdresseLivraison adresseLivraison;

    private Double totalProduits;
    private Double fraisLivraison;
    private Double totalCommande;

    private String modePaiement;
    private String modeLivraison;
    private String statut;

    private LocalDateTime dateCommande;

    private List<CommandeLigneResponseDTO> lignes;
}