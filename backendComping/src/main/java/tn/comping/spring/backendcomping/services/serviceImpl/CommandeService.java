package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.CommandeRequestDTO;
import tn.comping.spring.backendcomping.dto.CommandeResponseDTO;

import java.util.List;

public interface CommandeService {

    CommandeResponseDTO createCommande(CommandeRequestDTO commande);

    List<CommandeResponseDTO> getAllCommandes();

    List<CommandeResponseDTO> getCommandesByUser(String userId);

    CommandeResponseDTO updateStatut(String commandeId, String statut);

    void deleteCommande(String id);

    CommandeResponseDTO getCommandeById(String id);
}