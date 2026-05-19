package tn.comping.spring.backendcomping.entities;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "activity")
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    private String idActivity;
    private String nom;
    private String description;
    private String type;
    private String duree ;
    private String capacite;

    // ✅ AJOUTS POUR L'IA
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double prix;
    private String lieu;
    private double latitude;
    private double longitude;
    private String saison;
    private String meteoRecommandee;

    public String getIdActivity() { return idActivity; }
    public void setIdActivity(String idActivity) { this.idActivity = idActivity; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public String getCapacite() { return capacite; }
    public void setCapacite(String capacite) { this.capacite = capacite; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
    public String getTrancheAge() { return trancheAge; }
    public void setTrancheAge(String trancheAge) { this.trancheAge = trancheAge; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }
    public String getMeteoRecommandee() { return meteoRecommandee; }
    public void setMeteoRecommandee(String meteoRecommandee) { this.meteoRecommandee = meteoRecommandee; }
}
