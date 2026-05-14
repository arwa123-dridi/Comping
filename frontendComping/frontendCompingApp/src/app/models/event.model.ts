export interface Event {
  idEvent?: string;

  titre: string;
  description: string;
  prix: number;
  capacite: number;

  dateDebut: string;      // LocalDateTime -> string ISO format
  dateFin: string;        // LocalDateTime -> string ISO format

  statut: string;
  lieu: string;

  organisateurId: string;
  participantIds: string[];

  imageUrl: string;
  categorie: string;

  createdAt: string;      // LocalDateTime -> string ISO format

  activityIds: string[];

  // Attributs IA
  tags: string[];
  niveauDifficulte: string;
  trancheAge: string;

  latitude: number;
  longitude: number;

  saison: string;
  dureeEnHeures: number;

  // Si tu veux garder les objets activities détaillés
  activities?: {
    idActivity: string;
    nom: string;
  }[];
}