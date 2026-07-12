package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.HistoriqueEntry;
import tn.comping.spring.backendcomping.entities.PrioriteIncident;
import tn.comping.spring.backendcomping.entities.StatutIncident;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {
    private String idIncident;
    private String type;
    private StatutIncident statut;
    private String description;
    private Date dateDeclaration;
    private boolean resolu;
    private String userId;

    private PrioriteIncident priorite;
    private String commentaireOrganisateur;
    private LocalDateTime dateTraitement;
    private String demandeTransportId;

    private List<HistoriqueEntry> historique;
}
