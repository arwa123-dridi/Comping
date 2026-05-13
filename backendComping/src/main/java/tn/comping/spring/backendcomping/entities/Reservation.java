package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data @NoArgsConstructor @AllArgsConstructor
@Document(collection = "Reservation")
public class Reservation {
    @Id
    private String id;
    private String siteCampingId;
    private String utilisateurId;
    private Date dateDebut;
    private Date dateFin;
    private StatutReservation statut;
    private double montantTotal;
    private String modePaiement;
    private Date datePaiement;
    private String statutPaiement;

    private String paiementId;
}