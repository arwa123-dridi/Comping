package tn.comping.spring.backendcomping.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {

    private String nom;
    private String description;
    private String type;
    private String duree;
    private String capacite;
    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private List<String> tags;

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
    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
    public String getTrancheAge() { return trancheAge; }
    public void setTrancheAge(String trancheAge) { this.trancheAge = trancheAge; }
    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}


