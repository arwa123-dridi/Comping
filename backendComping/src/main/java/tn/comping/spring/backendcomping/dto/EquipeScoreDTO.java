package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Equipe;

@NoArgsConstructor
@AllArgsConstructor
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

    public Equipe getEquipe() { return equipe; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public int getScorePercent() { return scorePercent; }
    public void setScorePercent(int scorePercent) { this.scorePercent = scorePercent; }
    public String getRaisonPrincipale() { return raisonPrincipale; }
    public void setRaisonPrincipale(String raisonPrincipale) { this.raisonPrincipale = raisonPrincipale; }
    public int getPlacesLibres() { return placesLibres; }
    public void setPlacesLibres(int placesLibres) { this.placesLibres = placesLibres; }
    public boolean isEstPopulaire() { return estPopulaire; }
    public void setEstPopulaire(boolean estPopulaire) { this.estPopulaire = estPopulaire; }
}
