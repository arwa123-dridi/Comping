package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;
import tn.comping.spring.backendcomping.entities.TypeCible;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
public class AvisResponseDTO {

    private String id;
    private int note;
    private String commentaire;
    private Date datePublication;
    private StatutAvis statut;
    private boolean valide;

    private String utilisateurId;
    private String utilisateurNom;
    private String cibleId;
    private TypeCible typeCible;

    private ReponseAvisDTO reponse;
    private Date dateModification;

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
    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }
    public String getCibleId() { return cibleId; }
    public void setCibleId(String cibleId) { this.cibleId = cibleId; }
    public TypeCible getTypeCible() { return typeCible; }
    public void setTypeCible(TypeCible typeCible) { this.typeCible = typeCible; }
    public ReponseAvisDTO getReponse() { return reponse; }
    public void setReponse(ReponseAvisDTO reponse) { this.reponse = reponse; }
    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }
}