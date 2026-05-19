package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commandes")
public class CommandeProduct {

    @Id
    private String id;

    private String userId;
    private String livreurId;
    private List<CommandeLigne> lignes;

    // pricing
    private Double totalProduits;
    private Double fraisLivraison;
    private Double totalCommande;

    // choices
    private ModePaiement modePaiement;
    private ModeLivraison modeLivraison;

    private StatutCommande statutCommande;

    private AdresseLivraison adresseLivraison;

    private LocalDateTime dateCommande;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLivreurId() { return livreurId; }
    public void setLivreurId(String livreurId) { this.livreurId = livreurId; }
    public List<CommandeLigne> getLignes() { return lignes; }
    public void setLignes(List<CommandeLigne> lignes) { this.lignes = lignes; }
    public Double getTotalProduits() { return totalProduits; }
    public void setTotalProduits(Double totalProduits) { this.totalProduits = totalProduits; }
    public Double getFraisLivraison() { return fraisLivraison; }
    public void setFraisLivraison(Double fraisLivraison) { this.fraisLivraison = fraisLivraison; }
    public Double getTotalCommande() { return totalCommande; }
    public void setTotalCommande(Double totalCommande) { this.totalCommande = totalCommande; }
    public ModePaiement getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiement modePaiement) { this.modePaiement = modePaiement; }
    public ModeLivraison getModeLivraison() { return modeLivraison; }
    public void setModeLivraison(ModeLivraison modeLivraison) { this.modeLivraison = modeLivraison; }
    public StatutCommande getStatutCommande() { return statutCommande; }
    public void setStatutCommande(StatutCommande statutCommande) { this.statutCommande = statutCommande; }
    public AdresseLivraison getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(AdresseLivraison adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
}
