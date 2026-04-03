// src/app/models/sortie.model.ts
export interface SortieResponse {
    id: string;
    titre: string;
    description: string;
    dateDebut: Date;
    dateFin?: Date;
    lieuDepart: string;
    lieuArrivee?: string;
    region?: string;
    difficulte: 'FACILE' | 'MOYEN' | 'DIFFICILE';
    capaciteMax: number;
    placesDisponibles: number;
    prixParPersonne?: number;
    statut: string;
    organisateurId: string;
    organisateurNom: string;
    equipeId?: string;
    equipeNom?: string;
    nombreParticipants: number;
    participantIds?: string[];
    dateCreation: Date;
    equipementRequis?: string;
    assistanceMedicale?: boolean;
    distanceKm?: number;
}

export interface SortieRequest {
    titre: string;
    description: string;
    dateDebut: Date | string;
    dateFin?: Date | string;
    lieuDepart: string;
    lieuArrivee?: string;
    region?: string;
    difficulte: 'FACILE' | 'MOYEN' | 'DIFFICILE';
    capaciteMax: number;
    prixParPersonne?: number;
    equipementRequis?: string;
    assistanceMedicale?: boolean;
    organisateurId: string;
    organisateurNom: string;
    equipeId?: string;
    distanceKm?: number;
}