import { EquipeResponse } from './equipe.model';

export interface EquipeScoreDTO {
  equipe: EquipeResponse;
  score: number;
  scorePercent: number;
  raisonPrincipale: string;
  placesLibres: number;
  estPopulaire: boolean;
}