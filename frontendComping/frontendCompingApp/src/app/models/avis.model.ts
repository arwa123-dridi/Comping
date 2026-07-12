export interface AvisRequest {
  note: number;
  commentaire: string;
  cibleId: string;
  typeCible: string;
}

export interface AvisResponse {
  id: string;
  note: number;
  commentaire: string;
  datePublication: string;
  statut: string;
  valide: boolean;
  utilisateurId: string;
  utilisateurNom: string;
  cibleId: string;
  typeCible: string;
}
