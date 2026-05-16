package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Sortie;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortieScoreDTO {

    private Sortie sortie;
    private double score;           
    private int    scorePercent;    
    private String raisonPrincipale; 
    private boolean estPopulaire;   
    private int placesLibres;

    public SortieScoreDTO(Sortie sortie, double score, String raison) {
        this.sortie           = sortie;
        this.score            = score;
        this.scorePercent     = (int) Math.round(score * 100);
        this.raisonPrincipale = raison;
        this.estPopulaire     = false;

        if (sortie != null) {
            int max   = sortie.getCapaciteMax() != null ? sortie.getCapaciteMax() : 0;
            int inscrits = sortie.getParticipantIds() != null
                    ? sortie.getParticipantIds().size() : 0;
            this.placesLibres = Math.max(0, max - inscrits);
        }
    }
}
