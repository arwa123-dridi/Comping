package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EquipeScoreDTO;
import tn.comping.spring.backendcomping.dto.SortieScoreDTO;
import tn.comping.spring.backendcomping.entities.UserProfile;

import java.util.List;

public interface IRecommandationService {

    // Recommander des sorties selon l'historique de l'utilisateur
    List<SortieScoreDTO> recommanderSorties(String utilisateurId);

    // Recommander des équipes selon le profil de l'utilisateur
    List<EquipeScoreDTO> recommanderEquipes(String utilisateurId);

    // Construire ou récupérer le profil d'un utilisateur
    UserProfile construireOuMettreAJourProfil(String utilisateurId);

    // Appeler après chaque inscription à une sortie
    void mettreAJourProfilApresInscription(String utilisateurId);

}