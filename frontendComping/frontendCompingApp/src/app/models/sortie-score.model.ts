import { SortieResponse } from './sortie.model';

export interface SortieScoreDTO {
  sortie: SortieResponse;
  score: number;
  scorePercent: number;
  raisonPrincipale: string;
  placesLibres: number;
  estPopulaire: boolean;
}