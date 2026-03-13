package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import java.util.List;

public interface IEquipeService {
    // CRUD de base
    EquipeResponseDTO createEquipe(EquipeRequestDTO dto);
    EquipeResponseDTO getEquipeById(String id);
    List<EquipeResponseDTO> getAllEquipes();
    EquipeResponseDTO updateEquipe(String id, EquipeRequestDTO dto);
    void deleteEquipe(String id);

    // Gestion des membres
    EquipeResponseDTO ajouterMembre(String equipeId, String utilisateurId, String utilisateurNom);

    EquipeResponseDTO ajouterMembre(String equipeId, String utilisateurId);

    EquipeResponseDTO retirerMembre(String equipeId, String utilisateurId);
    List<String> getMembresByEquipe(String equipeId);

    // Recherches
    List<EquipeResponseDTO> getEquipesAvecPlace();
}