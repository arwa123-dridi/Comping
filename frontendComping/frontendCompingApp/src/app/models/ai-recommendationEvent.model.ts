export interface EventRecommandation {
  idEvent: string;
  titre: string;
  lieu: string;
  prix: number;
  scoreMatch: number;
  raison: string;
}
export interface AiRecommendation {
  recommendations: EventRecommandation[];
}