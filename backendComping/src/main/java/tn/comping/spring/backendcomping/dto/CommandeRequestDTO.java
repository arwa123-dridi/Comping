package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.AdresseLivraison;
import tn.comping.spring.backendcomping.entities.ModeLivraison;
import tn.comping.spring.backendcomping.entities.ModePaiement;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandeRequestDTO {

    private String userId;

    private String prenom;
    private String nom;

    private String email;
    private String telephone;

    private AdresseLivraison adresseLivraison;
    

    private ModePaiement modePaiement;
    private ModeLivraison modeLivraison;

    // cart snapshot sent from frontend
    private List<CommandeLigneRequestDTO> lignes;
}