package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.dto.StatistiquesAvisDTO;
import tn.comping.spring.backendcomping.entities.StatutAvis;

import java.util.List;

public interface AvisService {

    AvisResponseDTO creerAvis(AvisRequestDTO dto, String utilisateurEmail);

    AvisResponseDTO getAvisById(String id);

    List<AvisResponseDTO> getAvisByCible(String cibleId, String typeCible);

    List<AvisResponseDTO> getMesAvis(String utilisateurEmail);

    List<AvisResponseDTO> getAvisByStatut(StatutAvis statut);

    AvisResponseDTO updateAvis(String id, AvisRequestDTO dto, String utilisateurEmail);

    void deleteAvis(String id, String utilisateurEmail);

    AvisResponseDTO validerAvis(String id, String adminEmail);

    AvisResponseDTO rejeterAvis(String id, String motif, String adminEmail);

    StatistiquesAvisDTO getStatistiquesAvis(String cibleId, String typeCible);

    List<AvisResponseDTO> getAvisValides();

    List<AvisResponseDTO> getAvisAmis(String utilisateurEmail);
}
