package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.InscriptionRequest;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.entities.Difficulte;

import java.util.List;

    // CRUD de base
    public interface ISortieService {
        SortieResponseDTO createSortie(SortieRequestDTO dto);
        SortieResponseDTO getSortieById(String id);
        List<SortieResponseDTO> getAllSorties();
        SortieResponseDTO updateSortie(String id, SortieRequestDTO dto);
        void deleteSortie(String id);

        // ✅ Ajouter juste cette ligne
        SortieResponseDTO dissocierEquipe(String id);

        ParticipationDTO inscrireParticipant(String sortieId, InscriptionRequest request);
        void desinscrireParticipant(String sortieId, String utilisateurId);
        List<ParticipationDTO> getParticipantsBySortie(String sortieId);
        List<SortieResponseDTO> getSortiesByOrganisateur(String organisateurId);
        List<SortieResponseDTO> getSortiesByDifficulte(Difficulte difficulte);
        List<SortieResponseDTO> getProchainesSorties();
    }