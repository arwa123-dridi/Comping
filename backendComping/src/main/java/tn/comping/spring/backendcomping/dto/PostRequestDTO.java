package tn.comping.spring.backendcomping.dto;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {
    private String avisId;
    private String cibleType;
    private String cibleId;
    private String contenu;
    private List<String> images;
    private String visibilite; // PUBLIC, AMIS, PRIVE

    public String getAvisId() { return avisId; }
    public void setAvisId(String avisId) { this.avisId = avisId; }
    public String getCibleType() { return cibleType; }
    public void setCibleType(String cibleType) { this.cibleType = cibleType; }
    public String getCibleId() { return cibleId; }
    public void setCibleId(String cibleId) { this.cibleId = cibleId; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getVisibilite() { return visibilite; }
    public void setVisibilite(String visibilite) { this.visibilite = visibilite; }
}
