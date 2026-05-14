import { StatutCommande } from "./statut-commande";

export interface Commande {
  id: string;
  userId: string;
  totalProduits: number;
  fraisLivraison: number;
  totalCommande: number;
  modePaiement: string;
  modeLivraison: string;
  statutCommande: StatutCommande;
  dateCommande: Date;

}

