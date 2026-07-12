package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import tn.comping.spring.backendcomping.entities.StatutIncident;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraitementIncidentRequest {
    private StatutIncident statut;
    private String commentaireOrganisateur;
}
