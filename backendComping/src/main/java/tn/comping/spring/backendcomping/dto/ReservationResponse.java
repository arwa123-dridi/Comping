package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.StatutReservation;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
public class ReservationResponse {
    private String id;
    private String siteCampingId;
    private String utilisateurId;
    private Date dateDebut;
    private Date dateFin;
    private StatutReservation statut;
    private double montantTotal;
    private String modePaiement;
}