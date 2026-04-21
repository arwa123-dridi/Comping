package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Equipe;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipeScoreDTO {

    private Equipe equipe;
    private double score;
    private int    scorePercent;
    private String raisonPrincipale;
    private int    placesLibres;
    private boolean estPopulaire;

    public EquipeScoreDTO(Equipe equipe, double score, String raison) {
        this.equipe           = equipe;
        this.score            = score;
        this.scorePercent     = (int) Math.round(score * 100);
        this.raisonPrincipale = raison;
        this.estPopulaire     = false;

        if (equipe != null) {
            int max     = equipe.getNbMembresMax() != null ? equipe.getNbMembresMax() : 10;
            int membres = equipe.getMembres()      != null ? equipe.getMembres().size() : 0;
            this.placesLibres = Math.max(0, max - membres);
        }
    }
}
