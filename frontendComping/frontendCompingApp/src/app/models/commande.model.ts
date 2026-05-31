import { AdresseLivraison } from "../services/CommandeService";
import { StatutCommande } from "./statut-commande";
import { User } from "./user.model";

export interface Commande {
  id: string;
  userId: string;
  totalProduits: number;
  fraisLivraison: number;
  totalCommande: number;
  modePaiement: string;
  modeLivraison: string;
  statutCommande: string;
  dateCommande: Date;
  livreurId?: string;
  livreur?: User;
  adresseLivraison?: AdresseLivraison;
   // 🚚 LIVREUR

  livreurNom?: string;
  livreurEmail?: string;
  lignes?: CommandeLigne[];
}


export interface CommandeLigne {
  produitId: string;
  nomProduit: string;
  imageUrl: string;
  prixUnitaire: number;
  quantite: number;
  sousTotal: number;
}
