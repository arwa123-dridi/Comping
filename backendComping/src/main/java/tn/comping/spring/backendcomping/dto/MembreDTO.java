package tn.comping.spring.backendcomping.dto;

import lombok.Data;

@Data
public class MembreDTO {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private Boolean estOrganisateur;

    public void setPrenom(String firstName) {
        this.prenom = firstName;
    }
}
