package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvisResponseDTO {

    private String id;
    private int note;
    private String commentaire;
    private Date datePublication;
    private StatutAvis statut;
    private boolean valide;

    private String utilisateurId;
    private String utilisateurNom;
    private String cibleId;
    private TypeCible typeCible;

    private ReponseAvisDTO reponse;
    private Date dateModification;
}