package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Paiement")
public class Paiement {

    @Id
    private String id;

    private double montant;
    private Date datePaiement;
    private StatutPaiement statut;
    private String methode;

    // Relation One-to-One avec Reservation
    private String reservationId;

    // Stripe
    private String stripePaymentIntentId;
    private String stripeClientSecret;

    // Méthodes métier
    public void valider() {
        this.statut = this.statut.valider();
        this.datePaiement = new Date();
    }

    public void rembourser() {
        this.statut = this.statut.rembourser();
    }
}