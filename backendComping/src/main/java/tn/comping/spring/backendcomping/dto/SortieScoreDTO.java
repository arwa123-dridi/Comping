package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Sortie;

@NoArgsConstructor
@AllArgsConstructor
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

    public Sortie getSortie() { return sortie; }
    public void setSortie(Sortie sortie) { this.sortie = sortie; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public int getScorePercent() { return scorePercent; }
    public void setScorePercent(int scorePercent) { this.scorePercent = scorePercent; }
    public String getRaisonPrincipale() { return raisonPrincipale; }
    public void setRaisonPrincipale(String raisonPrincipale) { this.raisonPrincipale = raisonPrincipale; }
    public boolean isEstPopulaire() { return estPopulaire; }
    public void setEstPopulaire(boolean estPopulaire) { this.estPopulaire = estPopulaire; }
    public int getPlacesLibres() { return placesLibres; }
    public void setPlacesLibres(int placesLibres) { this.placesLibres = placesLibres; }
}
