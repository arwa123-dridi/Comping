package tn.comping.spring.backendcomping.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "events")
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    private String idEvent;

    private String titre;
    private String description;
    private double prix;
    private int capacite;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutEvent statut;
    private String lieu;
    private String organisateurId;
    private List<String> participantIds;
    private String imageUrl;
    private String categorie;
    private LocalDateTime createdAt;
    private List<String> activityIds;

    // AJOUTS POUR L'IA
    private List<String> tags;
    private String niveauDifficulte;
    private String trancheAge;
    private double latitude;
    private double longitude;
    private String saison;
    private int dureeEnHeures;
    private int pointsRecompense = 50;

    // Manual Getters and Setters
    public String getIdEvent() { return idEvent; }
    public void setIdEvent(String idEvent) { this.idEvent = idEvent; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public StatutEvent getStatut() { return statut; }
    public void setStatut(StatutEvent statut) { this.statut = statut; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getOrganisateurId() { return organisateurId; }
    public void setOrganisateurId(String organisateurId) { this.organisateurId = organisateurId; }
    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<String> getActivityIds() { return activityIds; }
    public void setActivityIds(List<String> activityIds) { this.activityIds = activityIds; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
    public String getTrancheAge() { return trancheAge; }
    public void setTrancheAge(String trancheAge) { this.trancheAge = trancheAge; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getSaison() { return saison; }
    public void setSaison(String saison) { this.saison = saison; }
    public int getDureeEnHeures() { return dureeEnHeures; }
    public void setDureeEnHeures(int dureeEnHeures) { this.dureeEnHeures = dureeEnHeures; }
    public int getPointsRecompense() { return pointsRecompense; }
    public void setPointsRecompense(int pointsRecompense) { this.pointsRecompense = pointsRecompense; }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Event event = new Event();
        public EventBuilder idEvent(String idEvent) { event.setIdEvent(idEvent); return this; }
        public EventBuilder titre(String titre) { event.setTitre(titre); return this; }
        public EventBuilder description(String description) { event.setDescription(description); return this; }
        public EventBuilder dateDebut(LocalDateTime dateDebut) { event.setDateDebut(dateDebut); return this; }
        public EventBuilder dateFin(LocalDateTime dateFin) { event.setDateFin(dateFin); return this; }
        public EventBuilder lieu(String lieu) { event.setLieu(lieu); return this; }
        public Event build() { return event; }
    }
}
