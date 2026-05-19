package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.entities.Difficulte;
import java.util.List;

public interface ISortieService {
    // CRUD de base
    SortieResponseDTO createSortie(SortieRequestDTO dto);
    SortieResponseDTO getSortieById(String id);
    List<SortieResponseDTO> getAllSorties();
    SortieResponseDTO updateSortie(String id, SortieRequestDTO dto);
    void deleteSortie(String id);

    // Gestion des participants
    ParticipationDTO inscrireParticipant(String sortieId, String utilisateurId, String utilisateurNom, String utilisateurEmail);
    void desinscrireParticipant(String sortieId, String utilisateurId);
    List<ParticipationDTO> getParticipantsBySortie(String sortieId);

    // Recherches spécifiques
    List<SortieResponseDTO> getSortiesByOrganisateur(String organisateurId);
    List<SortieResponseDTO> getSortiesByDifficulte(Difficulte difficulte);
    List<SortieResponseDTO> getProchainesSorties();
}