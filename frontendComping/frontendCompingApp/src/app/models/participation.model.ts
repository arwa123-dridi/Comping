export interface ParticipationDTO {
    id: string;
    utilisateurId: string;
    utilisateurNom: string;
    sortieId: string;
    sortieTitre: string;
    dateInscription: Date;
    statutPresence: string;
    aValideChecklist: boolean;
}