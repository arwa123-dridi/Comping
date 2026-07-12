export interface HistoriqueEntry {
  date: string;
  statutPrecedent: string | null;
  statutNouveau: string;
  commentaire?: string;
  auteurId?: string;
}
