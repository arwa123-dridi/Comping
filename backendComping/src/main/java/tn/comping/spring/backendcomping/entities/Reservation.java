package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
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
    private Integer nombrePersonnes=1;

    private String paiementId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSiteCampingId() { return siteCampingId; }
    public void setSiteCampingId(String siteCampingId) { this.siteCampingId = siteCampingId; }
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }
    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }
    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    public Date getDatePaiement() { return datePaiement; }
    public void setDatePaiement(Date datePaiement) { this.datePaiement = datePaiement; }
    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }
    public Integer getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(Integer nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }
    public String getPaiementId() { return paiementId; }
    public void setPaiementId(String paiementId) { this.paiementId = paiementId; }
}