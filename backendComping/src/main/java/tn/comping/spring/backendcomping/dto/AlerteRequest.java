package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.TypeAlerte;

@NoArgsConstructor 
@AllArgsConstructor
public class AlerteRequest {
    private String siteCampingId;
    private TypeAlerte type;
    private String titre;
    private String description;
    private String position;

    public String getSiteCampingId() { return siteCampingId; }
    public void setSiteCampingId(String siteCampingId) { this.siteCampingId = siteCampingId; }
    public TypeAlerte getType() { return type; }
    public void setType(TypeAlerte type) { this.type = type; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}