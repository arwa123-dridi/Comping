export interface SiteCamping {
  id: string;
  nom: string;
  tarifs: number;
  localisation: string;
  description: string;
  latitude: number;
  longitude: number;
  capacite: number;
  disponible: boolean;
  consignesSecurite: string;
  photos: string[];
  noteMoyenne?: number;
  proprietaireId: string;
}