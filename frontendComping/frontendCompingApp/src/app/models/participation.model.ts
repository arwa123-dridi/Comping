export interface ParticipationDTO {
  id?: string;
  equipeId?: string;
  equipeNom?: string;
  utilisateurId?: string;
  utilisateurNom?: string;
  utilisateurPrenom?: string;
  utilisateurEmail?: string;
  sortieId?: string;
  sortieTitre?: string;
  dateInscription?: Date;
  statutPresence?: string;
  aValideChecklist?: boolean;
  message?: string;
}
