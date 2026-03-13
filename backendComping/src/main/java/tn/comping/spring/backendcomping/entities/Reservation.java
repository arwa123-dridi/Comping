package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Reservation")
public class Reservation {

    @Id
    private String  id;
    private Date dateDebut;
    private Date dateFin;
    private String statut;   
    private double montantTotal;
    private String modePaiement;  
    private Date datePaiement;
    private String statutPaiement;

    
}
