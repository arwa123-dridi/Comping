package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.EquipeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // ── Créer une équipe ──────────────────────────────────────
    @Override
    public EquipeResponseDTO createEquipe(EquipeRequestDTO dto) {
        SignupEntity organisateur = signupRepository.findById(dto.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé: " + dto.getOrganisateurId()));

        Equipe equipe = EquipeMapper.toEntity(dto);
        equipe.setOrganisateur(organisateur);

        // L'organisateur est automatiquement le premier membre
        List<SignupEntity> membres = new ArrayList<>();
        membres.add(organisateur);
        equipe.setMembres(membres);

        Equipe saved = equipeRepository.save(equipe);
        log.info("Équipe créée: {} par {}", saved.getId(), organisateur.getId());
        return EquipeMapper.toDto(saved);
    }

    // ── Récupérer une équipe par ID ───────────────────────────
    @Override
    public EquipeResponseDTO getEquipeById(String id) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée: " + id));
        return EquipeMapper.toDto(equipe);
    }

    // ── Récupérer toutes les équipes (safe mapping) ───────────
    @Override
    public List<EquipeResponseDTO> getAllEquipes() {
        try {
            return equipeRepository.findAll().stream()
                    .map(e -> {
                        try {
                            return EquipeMapper.toDto(e);
                        } catch (Exception ex) {
                            // ✅ Mapping partiel si @DBRef corrompu — évite 500
                            log.warn("Erreur mapping equipe {}: {}", e.getId(), ex.getMessage());
                            EquipeResponseDTO dto = new EquipeResponseDTO();
                            dto.setId(e.getId());
                            dto.setNom(e.getNom() != null ? e.getNom() : "Équipe sans nom");
                            dto.setDescription(e.getDescription());
                            dto.setNbMembresMax(e.getNbMembresMax());
                            dto.setNiveau(e.getNiveau());
                            dto.setNbMembresActuels(0);
                            dto.setMembres(new ArrayList<>());
                            return dto;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur getAllEquipes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Modifier une équipe ───────────────────────────────────
    @Override
    public EquipeResponseDTO updateEquipe(String id, EquipeRequestDTO dto) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée: " + id));

        // ✅ FIX 3 : updateEntity met à jour nom/description/nbMembresMax/niveau seulement
        // Les membres sont gérés par ajouterMembre/retirerMembre — pas ici
        EquipeMapper.updateEntity(equipe, dto);

        Equipe updated = equipeRepository.save(equipe);
        log.info("Équipe {} mise à jour", id);
        return EquipeMapper.toDto(updated);
    }

    // ── Supprimer une équipe ──────────────────────────────────
    @Override
    public void deleteEquipe(String id) {
        // Détacher l'équipe de toutes les sorties (ne pas supprimer les sorties)
        List<Sortie> sorties = sortieRepository.findByEquipeId(id);
        sorties.forEach(s -> s.setEquipe(null));
        sortieRepository.saveAll(sorties);

        equipeRepository.deleteById(id);
        log.info("Équipe {} supprimée ({} sorties détachées)", id, sorties.size());
    }

    // ── Ajouter un membre ─────────────────────────────────────
    @Override
    public EquipeResponseDTO ajouterMembre(String equipeId, String utilisateurId, String utilisateurNom) {
        if (equipeId == null || utilisateurId == null) {
            throw new IllegalArgumentException("equipeId et utilisateurId sont obligatoires");
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée: " + equipeId));

        SignupEntity utilisateur = signupRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + utilisateurId));

        // Vérifier si déjà membre
        boolean dejaMembre = equipe.getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));
        if (dejaMembre) {
            throw new RuntimeException("Déjà membre de cette équipe");
        }

        // Vérifier la capacité
        if (equipe.getMembres().size() >= equipe.getNbMembresMax()) {
            throw new RuntimeException("Équipe pleine — capacité maximale atteinte");
        }

        equipe.getMembres().add(utilisateur);
        Equipe updated = equipeRepository.save(equipe);
        log.info("Membre {} ajouté à l'équipe {}", utilisateurId, equipeId);
        return EquipeMapper.toDto(updated);
    }

    // ── Retirer un membre ─────────────────────────────────────
    // ✅ FIX 1 : retourne void pour correspondre à IEquipeService et EquipeController


    @Override
    public EquipeResponseDTO retirerMembre(String equipeId, String utilisateurId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée: " + equipeId));

        boolean estMembre = equipe.getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));
        if (!estMembre) {
            throw new RuntimeException("L'utilisateur n'est pas membre de cette équipe");
        }

        // Empêcher de retirer l'organisateur
        if (equipe.getOrganisateur() != null &&
                equipe.getOrganisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("L'organisateur ne peut pas être retiré de son équipe");
        }

        equipe.getMembres().removeIf(m -> m.getId().equals(utilisateurId));
        Equipe updated = equipeRepository.save(equipe);
        log.info("Membre {} retiré de l'équipe {}", utilisateurId, equipeId);
        return EquipeMapper.toDto(updated);  // ← retourne EquipeResponseDTO comme l'interface
    }

    // ── Récupérer les IDs membres d'une équipe ────────────────
    @Override
    public List<String> getMembresByEquipe(String equipeId) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée: " + equipeId));
        return equipe.getMembres().stream()
                .map(SignupEntity::getId)
                .collect(Collectors.toList());
    }

    // ── Équipes avec places disponibles ──────────────────────
    @Override
    public List<EquipeResponseDTO> getEquipesAvecPlace() {
        return EquipeMapper.toDtoList(equipeRepository.findEquipesAvecPlace());
    }

    // ✅ FIX 2 : mapToResponseDTO() supprimé — dead code remplacé par EquipeMapper.toDto()
}