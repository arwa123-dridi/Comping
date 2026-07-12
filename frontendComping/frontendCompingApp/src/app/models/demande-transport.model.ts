import { HistoriqueEntry } from './historique.model';

export type StatutDemande = 'EN_ATTENTE' | 'PLANIFIEE' | 'EN_COURS' | 'LIVREE' | 'ANNULEE';

export interface DemandeTransportResponse {
  idDemandeTransport: string;
  dateCreation: string;
  statut: StatutDemande;
  typeService: string;
  userId: string;
  adresseDepart?: string;
  adresseArrivee?: string;
  dateSouhaitee?: string;
  description?: string;
  creneauLivraisonId?: string;
  commentaireOrganisateur?: string;
  dateTraitement?: string;
  livreurId?: string;
  historique: HistoriqueEntry[];
  noteAttribuee: boolean;
}

export interface DemandeTransportRequest {
  typeService: string;
  adresseDepart: string;
  adresseArrivee: string;
  dateSouhaitee: string;
  description: string;
}

export interface TraitementDemandeRequest {
  statut: StatutDemande;
  commentaireOrganisateur?: string;
  creneauLivraisonId?: string;
}

export interface CreneauSuggestion {
  creneauLivraisonId: string;
  heureDebut: string;
  heureFin: string;
  raison: string;
}
