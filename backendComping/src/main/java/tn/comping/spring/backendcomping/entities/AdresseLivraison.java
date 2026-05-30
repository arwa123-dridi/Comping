package tn.comping.spring.backendcomping.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdresseLivraison {

    private String prenom;
    private String nom;
    private String telephone;
    private String adresse;
    private String ville;
    private String codePostal;
}