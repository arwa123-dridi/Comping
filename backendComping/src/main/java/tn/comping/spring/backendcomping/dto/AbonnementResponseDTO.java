package tn.comping.spring.backendcomping.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class AbonnementResponseDTO {
    private String id;
    private String suiviId;
    private String suiviNom;
    private String suiviEmail;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSuiviId() { return suiviId; }
    public void setSuiviId(String suiviId) { this.suiviId = suiviId; }
    public String getSuiviNom() { return suiviNom; }
    public void setSuiviNom(String suiviNom) { this.suiviNom = suiviNom; }
    public String getSuiviEmail() { return suiviEmail; }
    public void setSuiviEmail(String suiviEmail) { this.suiviEmail = suiviEmail; }
}
