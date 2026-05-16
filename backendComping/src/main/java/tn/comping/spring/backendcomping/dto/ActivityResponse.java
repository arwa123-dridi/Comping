package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponse {
    private String idActivity;
    private String nom;
    private String description;
    private String type;
    private String duree;
    private String capacite;

    private String niveauDifficulte;
    private String trancheAge;
    private String saison;
    private List<String> tags;
}
