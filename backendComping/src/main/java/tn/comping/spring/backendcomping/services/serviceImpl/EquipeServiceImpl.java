package tn.comping.spring.backendcomping.services.impl;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.services.interfaces.IEquipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EquipeServiceImpl implements IEquipeService {

    private final EquipeRepository equipeRepository;
    private final SortieRepository sortieRepository;

    @Override
    public EquipeResponseDTO createEquipe(EquipeRequestDTO dto) {
        log.info("Création d'une équipe par l'organisateur: {}", dto.getOrganisateurId());

        Equipe equipe = new Equipe();
        equipe.setNom(dto.getNom());
        equipe.setDescription(dto.getDescription());
        equipe.setNbMembresMax(dto.getNbMembresMax() != null ? dto.getNbMembresMax() : 10);
        equipe.setNiveau(dto.getNiveau());
        equipe.setDateCreation(LocalDateTime.now());

        // L'organisateur est le premier membre
        equipe.setOrganisateurId(dto.getOrganisateurId());
        equipe.setOrganisateurNom(dto.getOrganisateurNom());

        List<String> membreIds = new ArrayList<>();
        membreIds.add(dto.getOrganisateurId());
        equipe.setMembreIds(membreIds);

        Equipe saved = equipeRepository.save(equipe);

        return mapToResponseDTO(saved);
    }

    @Override
    public EquipeResponseDTO getEquipeById(String id) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée avec l'id: " + id));
        return mapToResponseDTO(equipe);
    }

    @Override
    public List<EquipeResponseDTO> getAllEquipes() {
        return equipeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EquipeResponseDTO updateEquipe(String id, EquipeRequestDTO dto) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        equipe.setNom(dto.getNom());
        equipe.setDescription(dto.getDescription());
        if (dto.getNbMembresMax() != null) {
            equipe.setNbMembresMax(dto.getNbMembresMax());
        }
        equipe.setNiveau(dto.getNiveau());
        equipe.setDateModification(LocalDateTime.now());

        Equipe updated = equipeRepository.save(equipe);
        return mapToResponseDTO(updated);
    }

    @Override
    public void deleteEquipe(String id) {
        log.info("Suppression de l'équipe: {}", id);

        List<Sortie> sorties = sortieRepository.findByEquipeId(id);
        sorties.forEach(s -> {
            s.setEquipeId(null);
            s.setEquipeNom(null);
        });
        sortieRepository.saveAll(sorties);

        equipeRepository.deleteById(id);
        log.info("Équipe {} supprimée", id);
    }

    @Override
    public EquipeResponseDTO ajouterMembre(String equipeId, String utilisateurId, String utilisateurNom) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        if (equipe.getMembreIds().contains(utilisateurId)) {
            throw new RuntimeException("L'utilisateur est déjà membre de cette équipe");
        }

        if (equipe.getMembreIds().size() >= equipe.getNbMembresMax()) {
            throw new RuntimeException("Capacité maximale atteinte pour cette équipe");
        }

        equipe.getMembreIds().add(utilisateurId);
        Equipe updated = equipeRepository.save(equipe);

        return mapToResponseDTO(updated);
    }

    @Override
    public EquipeResponseDTO retirerMembre(String equipeId, String utilisateurId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        if (!equipe.getMembreIds().contains(utilisateurId)) {
            throw new RuntimeException("L'utilisateur n'est pas membre de cette équipe");
        }

        // L'organisateur ne peut pas être retiré
        if (utilisateurId.equals(equipe.getOrganisateurId())) {
            throw new RuntimeException("L'organisateur de l'équipe ne peut pas être retiré");
        }

        equipe.getMembreIds().remove(utilisateurId);
        Equipe updated = equipeRepository.save(equipe);

        return mapToResponseDTO(updated);
    }

    @Override
    public List<String> getMembresByEquipe(String equipeId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
        return equipe.getMembreIds();
    }

    @Override
    public List<EquipeResponseDTO> getEquipesAvecPlace() {
        return equipeRepository.findEquipesAvecPlace().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private EquipeResponseDTO mapToResponseDTO(Equipe equipe) {
        EquipeResponseDTO dto = new EquipeResponseDTO();
        dto.setId(equipe.getId());
        dto.setNom(equipe.getNom());
        dto.setDescription(equipe.getDescription());
        dto.setDateCreation(equipe.getDateCreation());
        dto.setNbMembresMax(equipe.getNbMembresMax());
        dto.setNbMembresActuels(equipe.getMembreIds() != null ? equipe.getMembreIds().size() : 0);
        dto.setNiveau(equipe.getNiveau());

        dto.setOrganisateurId(equipe.getOrganisateurId());
        dto.setOrganisateurNom(equipe.getOrganisateurNom());

        return dto;
    }
}