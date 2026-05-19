package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "avis")
@NoArgsConstructor
@AllArgsConstructor
public class Avis {

    @Id
    private String id;

    private int note;
    private String commentaire;
    private Date datePublication;

    private StatutAvis statut;
    private boolean valide;

    private String utilisateurId;

    // cible = site, event, etc
    private String cibleId;
    private TypeCible typeCible;

    private Date dateModification;

    // modération
    private String moderateurId;
    private String motifRejet;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Date getDatePublication() { return datePublication; }
    public void setDatePublication(Date datePublication) { this.datePublication = datePublication; }
    public StatutAvis getStatut() { return statut; }
    public void setStatut(StatutAvis statut) { this.statut = statut; }
    public boolean isValide() { return valide; }
    public void setValide(boolean valide) { this.valide = valide; }
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
    public String getCibleId() { return cibleId; }
    public void setCibleId(String cibleId) { this.cibleId = cibleId; }
    public TypeCible getTypeCible() { return typeCible; }
    public void setTypeCible(TypeCible typeCible) { this.typeCible = typeCible; }
    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }
    public String getModerateurId() { return moderateurId; }
    public void setModerateurId(String moderateurId) { this.moderateurId = moderateurId; }
    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }
}


    


