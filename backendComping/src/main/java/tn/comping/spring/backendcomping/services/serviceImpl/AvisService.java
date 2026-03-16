package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.*;
<<<<<<< HEAD
import java.util.List;

public interface AvisService {
    AvisResponse ajouterAvis(AvisRequest request);
    List<AvisResponse> getAvisBySite(String siteCampingId);
    List<AvisResponse> getAvisByUtilisateur(String utilisateurId);
    AvisResponse modererAvis(String id, String statut);
    void supprimerAvis(String id);
=======
import tn.comping.spring.backendcomping.entities.StatutAvis;

import java.util.List;

public interface AvisService {

    AvisResponseDTO creerAvis(AvisRequestDTO dto, String utilisateurEmail);
    AvisResponseDTO getAvisById(String id);
    List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCible);
    List<AvisResponseDTO> getMesAvis(String utilisateurEmail);
    List<AvisResponseDTO> getAvisByStatut(StatutAvis statut);
    AvisResponseDTO updateAvis(String id, AvisRequestDTO dto, String
utilisateurEmail);
    void deleteAvis(String id, String utilisateurEmail);

    // Modération
    AvisResponseDTO validerAvis(String id, String moderateurEmail);
    AvisResponseDTO rejeterAvis(String id, String motif, String
moderateurEmail);

    // Réponses
    AvisResponseDTO ajouterReponse(String avisId,
ReponseAvisRequestDTO dto, String auteurEmail);
    void supprimerReponse(String avisId, String auteurEmail);

    // Statistiques
    StatistiquesAvisDTO getStatistiquesAvis(String cibleId, String typeCible);
>>>>>>> origin/mariem-sellami
}