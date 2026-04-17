export interface Reservation {
  id:            string;
  siteCampingId: string;
  utilisateurId: string;
  dateDebut:     string;
  dateFin:       string;
  statut:        string;
  montantTotal:  number;
  modePaiement:  string;
}