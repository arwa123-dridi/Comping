package tn.comping.spring.backendcomping.dto;
import tn.comping.spring.backendcomping.dto.AlerteResponse;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;

import java.util.Date;

@NoArgsConstructor 
@AllArgsConstructor
public class AlerteResponse {
    private String id;
    private String siteCampingId;
    private TypeAlerte type;
    private String titre;
    private String description;
    private Date dateDeclenchement;
    private String statut;
    private String position;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSiteCampingId() { return siteCampingId; }
    public void setSiteCampingId(String siteCampingId) { this.siteCampingId = siteCampingId; }
    public TypeAlerte getType() { return type; }
    public void setType(TypeAlerte type) { this.type = type; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getDateDeclenchement() { return dateDeclenchement; }
    public void setDateDeclenchement(Date dateDeclenchement) { this.dateDeclenchement = dateDeclenchement; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}