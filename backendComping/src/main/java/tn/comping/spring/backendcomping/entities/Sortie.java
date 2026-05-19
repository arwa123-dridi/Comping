package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "sorties")
@NoArgsConstructor
@AllArgsConstructor
public class Sortie {

    @Id
    private String id;

    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieuDepart;
    private String lieuArrivee;
    private String region;
    private Difficulte difficulte;
    private Double distanceKm;
    private Integer capaciteMax;
    private Double prixParPersonne;
    private String equipementRequis;
    private Boolean assistanceMedicale;
    private StatutSortie statut;

    //  URL image Cloudinary (null si pas d'image)
    private String imageUrl;

    @DBRef
    private SignupEntity organisateur;

    @DBRef
    private Equipe equipe;

    private List<String> participantIds = new ArrayList<>();

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public String getLieuDepart() { return lieuDepart; }
    public void setLieuDepart(String lieuDepart) { this.lieuDepart = lieuDepart; }
    public String getLieuArrivee() { return lieuArrivee; }
    public void setLieuArrivee(String lieuArrivee) { this.lieuArrivee = lieuArrivee; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Difficulte getDifficulte() { return difficulte; }
    public void setDifficulte(Difficulte difficulte) { this.difficulte = difficulte; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }
    public Double getPrixParPersonne() { return prixParPersonne; }
    public void setPrixParPersonne(Double prixParPersonne) { this.prixParPersonne = prixParPersonne; }
    public String getEquipementRequis() { return equipementRequis; }
    public void setEquipementRequis(String equipementRequis) { this.equipementRequis = equipementRequis; }
    public Boolean getAssistanceMedicale() { return assistanceMedicale; }
    public void setAssistanceMedicale(Boolean assistanceMedicale) { this.assistanceMedicale = assistanceMedicale; }
    public StatutSortie getStatut() { return statut; }
    public void setStatut(StatutSortie statut) { this.statut = statut; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public SignupEntity getOrganisateur() { return organisateur; }
    public void setOrganisateur(SignupEntity organisateur) { this.organisateur = organisateur; }
    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }
    public List<String> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<String> participantIds) { this.participantIds = participantIds; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}