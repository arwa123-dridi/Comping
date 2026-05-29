// src/app/models/equipe.model.ts
export interface EquipeResponse {
    id: string;
    nom: string;
    description?: string;
    dateCreation: Date;
    nbMembresMax: number;
    nbMembresActuels: number;
    capaciteMax?: number;
    niveau?: string;
    organisateurId: string;
    organisateurNom: string;
    membres?: MembreDTO[];
    nombreSorties?: number;
    sortiesTitres?: string[];
}

export interface MembreDTO {
    id: string;
    nom: string;
    email: string;
    estOrganisateur: boolean;
}

export interface EquipeRequest {
    nom: string;
    description?: string;
    nbMembresMax?: number;
    niveau?: string;
    organisateurId: string;
    organisateurNom: string;
}
