package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reponses_avis")
public class ReponseAvis {

    @Id
    private String id;

    private String contenu;
    private Date dateReponse;

    private String avisId;
    private String auteurId;
    private String roleAuteur;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public Date getDateReponse() { return dateReponse; }
    public void setDateReponse(Date dateReponse) { this.dateReponse = dateReponse; }
    public String getAvisId() { return avisId; }
    public void setAvisId(String avisId) { this.avisId = avisId; }
    public String getAuteurId() { return auteurId; }
    public void setAuteurId(String auteurId) { this.auteurId = auteurId; }
    public String getRoleAuteur() { return roleAuteur; }
    public void setRoleAuteur(String roleAuteur) { this.roleAuteur = roleAuteur; }
}