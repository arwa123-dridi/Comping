package tn.comping.spring.backendcomping.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {

    private String token;
    private String nouveauMotDePasse;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String nouveauMotDePasse) { this.nouveauMotDePasse = nouveauMotDePasse; }
}

