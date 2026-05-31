package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.services.IEquipeService;
import org.springframework.transaction.annotation.Transactional;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.entities.SignupEntity;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.utils.mapper.EquipeMapper;

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

        return EquipeMapper.toDto(equipeRepository.save(equipe));
    }

    @Override
    public EquipeResponseDTO getEquipeById(String id) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        return EquipeMapper.toDto(equipe);
    }

    @Override
    public List<EquipeResponseDTO> getAllEquipes() {
        return EquipeMapper.toDtoList(equipeRepository.findAll());
    }

    @Override
    public EquipeResponseDTO updateEquipe(String id, EquipeRequestDTO dto) {

        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        EquipeMapper.updateEntity(equipe, dto);

        return EquipeMapper.toDto(equipeRepository.save(equipe));
    }

    @Override
    public void deleteEquipe(String id) {

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

        if (equipe.getMembres() == null) {
            equipe.setMembres(new ArrayList<>());
        }

        boolean dejaMembre = equipe.getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));

        if (dejaMembre) {
            throw new RuntimeException("Utilisateur déjà membre");
        }

        if (equipe.getNbMembresMax() != null &&
            equipe.getMembres().size() >= equipe.getNbMembresMax()) {
            throw new RuntimeException("Capacité maximale atteinte");
        }

        equipe.getMembres().add(utilisateur);

        return EquipeMapper.toDto(equipeRepository.save(equipe));
    }

    @Override
    public EquipeResponseDTO retirerMembre(String equipeId, String utilisateurId) {

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        if (equipe.getMembres() == null) {
            return EquipeMapper.toDto(equipe);
        }

        boolean estMembre = equipe.getMembres().stream()
                .anyMatch(m -> m.getId().equals(utilisateurId));

        if (!estMembre) {
            throw new RuntimeException("Utilisateur non membre");
        }

        if (equipe.getOrganisateur() != null &&
            equipe.getOrganisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("Organisateur ne peut pas être retiré");
        }

        equipe.getMembres().removeIf(m -> m.getId().equals(utilisateurId));

        return EquipeMapper.toDto(equipeRepository.save(equipe));
    }

    @Override
    public List<String> getMembresByEquipe(String equipeId) {

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        if (equipe.getMembres() == null) return List.of();

        return equipe.getMembres().stream()
                .map(SignupEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipeResponseDTO> getEquipesAvecPlace() {
        return EquipeMapper.toDtoList(equipeRepository.findEquipesAvecPlace());
    }
}