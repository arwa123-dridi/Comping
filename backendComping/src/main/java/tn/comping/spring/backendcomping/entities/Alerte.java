package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@NoArgsConstructor @AllArgsConstructor
@Builder
@Document(collection = "Alerte")
public class Alerte {
    @Id
    private String id;
    private TypeAlerte type;
    private String titre;
    private String description;
    private Date dateDeclenchement;
    private String statut;
    private String position;
    private String siteCampingId;
    private String resolution;
    private String priorite;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getSiteCampingId() { return siteCampingId; }
    public void setSiteCampingId(String siteCampingId) { this.siteCampingId = siteCampingId; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }
}
