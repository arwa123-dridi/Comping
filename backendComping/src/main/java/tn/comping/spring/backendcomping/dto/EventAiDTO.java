package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
public class EventAiDTO {
    private String idEvent;
    private String titre;
    private String description;
    private double prix;
    private String lieu;
    private String categorie;
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private int dureeEnHeures;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public String getIdEvent() { return idEvent; }
    public void setIdEvent(String idEvent) { this.idEvent = idEvent; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
    public String getTrancheAge() { return trancheAge; }
    public void setTrancheAge(String trancheAge) { this.trancheAge = trancheAge; }
    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }
    public int getDureeEnHeures() { return dureeEnHeures; }
    public void setDureeEnHeures(int dureeEnHeures) { this.dureeEnHeures = dureeEnHeures; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public static EventAiDTOBuilder builder() {
        return new EventAiDTOBuilder();
    }

    public static class EventAiDTOBuilder {
        private EventAiDTO dto = new EventAiDTO();
        public EventAiDTOBuilder idEvent(String idEvent) { dto.setIdEvent(idEvent); return this; }
        public EventAiDTOBuilder titre(String titre) { dto.setTitre(titre); return this; }
        public EventAiDTOBuilder description(String description) { dto.setDescription(description); return this; }
        public EventAiDTOBuilder prix(double prix) { dto.setPrix(prix); return this; }
        public EventAiDTOBuilder lieu(String lieu) { dto.setLieu(lieu); return this; }
        public EventAiDTOBuilder categorie(String categorie) { dto.setCategorie(categorie); return this; }
        public EventAiDTOBuilder tags(List<String> tags) { dto.setTags(tags); return this; }
        public EventAiDTOBuilder niveauDifficulte(String niveauDifficulte) { dto.setNiveauDifficulte(niveauDifficulte); return this; }
        public EventAiDTOBuilder trancheAge(String trancheAge) { dto.setTrancheAge(trancheAge); return this; }
        public EventAiDTOBuilder saison(String saison) { dto.setSaison(saison); return this; }
        public EventAiDTOBuilder dureeEnHeures(int dureeEnHeures) { dto.setDureeEnHeures(dureeEnHeures); return this; }
        public EventAiDTOBuilder dateDebut(LocalDateTime dateDebut) { dto.setDateDebut(dateDebut); return this; }
        public EventAiDTOBuilder dateFin(LocalDateTime dateFin) { dto.setDateFin(dateFin); return this; }
        public EventAiDTO build() { return dto; }
    }
}
