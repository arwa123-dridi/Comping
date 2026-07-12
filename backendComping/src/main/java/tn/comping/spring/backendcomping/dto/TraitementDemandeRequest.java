package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import tn.comping.spring.backendcomping.entities.StatutDemandeTransport;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitementDemandeRequest {
    private StatutDemandeTransport statut;
    private String commentaireOrganisateur;
    private String creneauLivraisonId;
}
