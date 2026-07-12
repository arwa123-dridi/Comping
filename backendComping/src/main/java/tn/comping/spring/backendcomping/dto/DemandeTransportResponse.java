package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.HistoriqueEntry;
import tn.comping.spring.backendcomping.entities.StatutDemandeTransport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeTransportResponse{

    private String idDemandeTransport;
    private Date dateCreation;
    private StatutDemandeTransport statut;
    private String typeService;
    private String userId;

    private String adresseDepart;
    private String adresseArrivee;
    private LocalDate dateSouhaitee;
    private String description;

    private String creneauLivraisonId;
    private String commentaireOrganisateur;
    private LocalDateTime dateTraitement;
    private String livreurId;

    private List<HistoriqueEntry> historique;
    private boolean noteAttribuee;
}
