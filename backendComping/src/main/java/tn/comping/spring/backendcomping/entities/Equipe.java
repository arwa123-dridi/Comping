package tn.comping.spring.backendcomping.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "equipes")
@NoArgsConstructor
@AllArgsConstructor
public class Equipe {

    @Id
    private String id;

    private String nom;
    private String description;

    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateModification = LocalDateTime.now();

    private Integer nbMembresMax;
    private String niveau;

    @DBRef
    private SignupEntity organisateur;

    @DBRef
    private List<SignupEntity> membres = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    public Integer getNbMembresMax() { return nbMembresMax; }
    public void setNbMembresMax(Integer nbMembresMax) { this.nbMembresMax = nbMembresMax; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public SignupEntity getOrganisateur() { return organisateur; }
    public void setOrganisateur(SignupEntity organisateur) { this.organisateur = organisateur; }
    public List<SignupEntity> getMembres() { return membres; }
    public void setMembres(List<SignupEntity> membres) { this.membres = membres; }
}