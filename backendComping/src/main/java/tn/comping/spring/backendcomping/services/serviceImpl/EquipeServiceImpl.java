package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.dto.MembreDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.comping.spring.backendcomping.utils.mapper.EquipeMapper;

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
    private final SignupRepository signupRepository;

    @Override
    public EquipeResponseDTO createEquipe(EquipeRequestDTO dto) {

        SignupEntity organisateur = signupRepository.findById(dto.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));

        Equipe equipe = EquipeMapper.toEntity(dto);

        equipe.setOrganisateur(organisateur);

        List<SignupEntity> membres = new ArrayList<>();
        membres.add(organisateur);

        equipe.setMembres(membres);

        Equipe saved = equipeRepository.save(equipe);

        return EquipeMapper.toDto(saved);
    }

    @Override
    public EquipeResponseDTO getEquipeById(String id) {

        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        return EquipeMapper.toDto(equipe);
    }

    @Override
    public List<EquipeResponseDTO> getAllEquipes() {

        return EquipeMapper.toDtoList(
                equipeRepository.findAll()
        );
    }

    @Override
    public EquipeResponseDTO updateEquipe(String id, EquipeRequestDTO dto) {

        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        EquipeMapper.updateEntity(equipe , dto);

        Equipe updated = equipeRepository.save(equipe);

        return EquipeMapper.toDto(updated);
    }

    @Override
    public void deleteEquipe(String id) {
        log.info("Suppression de l'équipe: {}", id);

        List<Sortie> sorties = sortieRepository.findByEquipeId(id);
        sorties.forEach(s -> s.setEquipe(null));
        sortieRepository.saveAll(sorties);

        equipeRepository.deleteById(id);
        log.info("Équipe {} supprimée", id);
    }

    @Override
    public EquipeResponseDTO ajouterMembre(String equipeId, String utilisateurId) {

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        SignupEntity utilisateur = signupRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean dejaMembre = equipe.getMembres()
                .stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));

        if (dejaMembre) {
            throw new RuntimeException("Utilisateur déjà membre");
        }

        if (equipe.getMembres().size() >= equipe.getNbMembresMax()) {
            throw new RuntimeException("Capacité maximale atteinte");
        }

        equipe.getMembres().add(utilisateur);

        Equipe updated = equipeRepository.save(equipe);

        return EquipeMapper.toDto(updated);
    }

    @Override
    public EquipeResponseDTO retirerMembre(String equipeId, String utilisateurId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        boolean estMembre = equipe.getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));

        if (!estMembre) {
            throw new RuntimeException("L'utilisateur n'est pas membre de cette équipe");
        }

        if (equipe.getOrganisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("L'organisateur de l'équipe ne peut pas être retiré");
        }

        equipe.getMembres().removeIf(m -> m.getId().equals(utilisateurId));
        Equipe updated = equipeRepository.save(equipe);
        return mapToResponseDTO(updated);
    }

    @Override
    public List<String> getMembresByEquipe(String equipeId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        return equipe.getMembres().stream()
                .map(SignupEntity::getId)
                .collect(Collectors.toList());
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
        dto.setNbMembresActuels(equipe.getMembres().size());
        dto.setNiveau(equipe.getNiveau());

        if (equipe.getOrganisateur() != null) {
            dto.setOrganisateurId(equipe.getOrganisateur().getId());
            dto.setOrganisateurNom(equipe.getOrganisateur().getLastName());
        }

        // Convertir les membres en MembreDTO
        List<MembreDTO> membresDTO = equipe.getMembres().stream()
                .map(user -> {
                    MembreDTO membre = new MembreDTO();
                    membre.setId(user.getId());
                    membre.setNom(user.getFirstName());
                    membre.setEmail(user.getEmail());
                    membre.setEstOrganisateur(user.getId().equals(equipe.getOrganisateur().getId()));
                    return membre;
                })
                .collect(Collectors.toList());
        dto.setMembres(membresDTO);

        return dto;
    }
}