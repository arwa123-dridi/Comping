

export interface Event {
  idEvent?: string;
  titre: string;
  description: string;
  prix: number;
  capacite: number;
  statut: string;
  activities?: {
  idActivity: string;
  nom: string;
}[];
}