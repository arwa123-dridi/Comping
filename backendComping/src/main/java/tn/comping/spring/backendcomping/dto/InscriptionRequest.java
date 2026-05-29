package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionRequest {
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurEmail;
}
