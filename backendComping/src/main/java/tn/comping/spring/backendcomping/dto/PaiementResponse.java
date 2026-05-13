package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.util.Date;

@Data
public class PaiementResponse {
    private String id;
    private double montant;
    private Date datePaiement;
    private String statut;
    private String methode;
    private String reservationId;
    private String stripeClientSecret;
}