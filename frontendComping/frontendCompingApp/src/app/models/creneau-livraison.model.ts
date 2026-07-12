export interface CreneauLivraison {
  idCreneauLivraison: string;
  heureDebut: string;
  heureFin: string;
  disponible: boolean;
}

export interface CreneauLivraisonRequest {
  heureDebut: string;
  heureFin: string;
  disponible: boolean;
}
