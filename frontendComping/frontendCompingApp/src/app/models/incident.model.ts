import { HistoriqueEntry } from './historique.model';

export type StatutIncident = 'OUVERT' | 'EN_COURS' | 'RESOLU' | 'REJETE';
export type PrioriteIncident = 'FAIBLE' | 'MOYENNE' | 'HAUTE' | 'CRITIQUE';

export interface IncidentResponse {
  idIncident: string;
  type: string;
  statut: StatutIncident;
  description: string;
  dateDeclaration: string;
  resolu: boolean;
  userId: string;
  priorite: PrioriteIncident;
  commentaireOrganisateur?: string;
  dateTraitement?: string;
  demandeTransportId?: string;
  historique: HistoriqueEntry[];
}

export interface IncidentRequest {
  type: string;
  description: string;
  demandeTransportId?: string;
}

export interface TraitementIncidentRequest {
  statut: StatutIncident;
  commentaireOrganisateur?: string;
}
