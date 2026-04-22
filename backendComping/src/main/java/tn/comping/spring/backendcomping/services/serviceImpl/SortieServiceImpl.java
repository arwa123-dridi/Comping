package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.ISortieService;
import tn.comping.spring.backendcomping.utils.mapper.SortieMapper;

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
public class SortieServiceImpl implements ISortieService {

    private final SortieRepository sortieRepository;
    private final EquipeRepository equipeRepository;
    private final ParticipationRepository participationRepository;
    private final SignupRepository signupRepository;

    @Override
    public SortieResponseDTO createSortie(SortieRequestDTO dto) {
        log.info("Création d'une sortie par l'organisateur: {}", dto.getOrganisateurId());

        // Récupérer et vérifier l'organisateur
        SignupEntity organisateur = signupRepository.findById(dto.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));

        if (organisateur.getRole() != Role.ORGANISATEUR && organisateur.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les organisateurs peuvent créer des sorties");
        }

        // Créer la sortie avec le mapper
        Sortie sortie = SortieMapper.toEntity(dto);
        sortie.setOrganisateur(organisateur);

        // Lier à une équipe si spécifiée
        if (dto.getEquipeId() != null && !dto.getEquipeId().isEmpty()) {
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
            sortie.setEquipe(equipe);
        }

        Sortie saved = sortieRepository.save(sortie);
        log.info("Sortie créée avec ID: {}", saved.getId());

        return SortieMapper.toDto(saved);
    }

    @Override
    public SortieResponseDTO getSortieById(String id) {
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée avec l'id: " + id));

        SortieResponseDTO dto = SortieMapper.toDto(sortie);

        // Ajouter les informations supplémentaires
        List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
        dto.setNombreParticipants(participations.size());
        dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

        List<String> participantIds = participations.stream()
                .map(p -> p.getUtilisateur().getId())
                .collect(Collectors.toList());
        dto.setParticipantIds(participantIds);

        return dto;
    }

    @Override
    public List<SortieResponseDTO> getAllSorties() {
        List<Sortie> sorties = sortieRepository.findAll();

        return sorties.stream()
                .map(sortie -> {
                    SortieResponseDTO dto = SortieMapper.toDto(sortie);

                    List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
                    dto.setNombreParticipants(participations.size());
                    dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

                    List<String> participantIds = participations.stream()
                            .filter(p -> p != null && p.getUtilisateur() != null)
                            .map(p -> p.getUtilisateur().getId())
                            .collect(Collectors.toList());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SortieResponseDTO updateSortie(String id, SortieRequestDTO dto) {
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));

        SortieMapper.updateEntity(sortie, dto);

        if (dto.getEquipeId() != null && !dto.getEquipeId().isEmpty()) {
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
            sortie.setEquipe(equipe);
        }

        Sortie updated = sortieRepository.save(sortie);
        return SortieMapper.toDto(updated);
    }

    @Override
    public void deleteSortie(String id) {
        log.info("Suppression de la sortie: {}", id);

        // Supprimer les participations associées
        participationRepository.deleteBySortieId(id);

        // Supprimer la sortie
        sortieRepository.deleteById(id);

        log.info("Sortie {} supprimée avec toutes ses participations", id);
    }

    @Override
    public ParticipationDTO inscrireParticipant(String sortieId, String utilisateurId,
                                                String utilisateurNom, String utilisateurEmail) {
        log.info("Inscription de l'utilisateur {} à la sortie {}", utilisateurId, sortieId);

        Sortie sortie = sortieRepository.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));

        SignupEntity utilisateur = signupRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si déjà inscrit
        if (participationRepository.findByUtilisateurIdAndSortieId(utilisateurId, sortieId).isPresent()) {
            throw new RuntimeException("L'utilisateur est déjà inscrit à cette sortie");
        }

        // Vérifier la capacité
        long nbInscrits = participationRepository.countBySortieId(sortieId);
        if (nbInscrits >= sortie.getCapaciteMax()) {
            throw new RuntimeException("Capacité maximale atteinte pour cette sortie");
        }

        // Créer la participation
        Participation participation = Participation.builder()
                .utilisateur(utilisateur)
                .sortie(sortie)
                .dateInscription(LocalDateTime.now())
                .statutPresence("CONFIRME")
                .aValideChecklist(false)
                .dateCreation(LocalDateTime.now())
                .build();

        Participation saved = participationRepository.save(participation);

        // Ajouter l'ID à la liste des participants de la sortie
        if (sortie.getParticipantIds() == null) {
            sortie.setParticipantIds(new ArrayList<>());
        }
        sortie.getParticipantIds().add(utilisateurId);
        sortieRepository.save(sortie);

        return SortieMapper.toParticipationDto(saved);
    }

    @Override
    public void desinscrireParticipant(String sortieId, String utilisateurId) {
        Participation participation = participationRepository
                .findByUtilisateurIdAndSortieId(utilisateurId, sortieId)
                .orElseThrow(() -> new RuntimeException("L'utilisateur n'est pas inscrit à cette sortie"));

        participationRepository.delete(participation);

        Sortie sortie = sortieRepository.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));
        sortie.getParticipantIds().remove(utilisateurId);
        sortieRepository.save(sortie);

        log.info("Utilisateur {} désinscrit de la sortie {}", utilisateurId, sortieId);
    }

    @Override
    public List<ParticipationDTO> getParticipantsBySortie(String sortieId) {
        List<Participation> participations = participationRepository.findBySortieId(sortieId);
        return SortieMapper.toParticipationDtoList(participations);
    }

    @Override
    public List<SortieResponseDTO> getSortiesByOrganisateur(String organisateurId) {
        List<Sortie> sorties = sortieRepository.findByOrganisateurId(organisateurId);

        return sorties.stream()
                .map(sortie -> {
                    SortieResponseDTO dto = SortieMapper.toDto(sortie);

                    List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
                    dto.setNombreParticipants(participations.size());
                    dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

                    List<String> participantIds = participations.stream()
                            .map(p -> p.getUtilisateur().getId())
                            .collect(Collectors.toList());
                    dto.setParticipantIds(participantIds);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SortieResponseDTO> getSortiesByDifficulte(Difficulte difficulte) {
        log.info("Recherche des sorties par difficulté: {}", difficulte);

        List<Sortie> sorties = sortieRepository.findAll().stream()
                .filter(s -> s.getDifficulte() == difficulte)
                .collect(Collectors.toList());

        return sorties.stream()
                .map(sortie -> {
                    SortieResponseDTO dto = SortieMapper.toDto(sortie);

                    List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
                    dto.setNombreParticipants(participations.size());
                    dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

                    List<String> participantIds = participations.stream()
                            .map(p -> p.getUtilisateur().getId())
                            .collect(Collectors.toList());
                    dto.setParticipantIds(participantIds);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SortieResponseDTO> getProchainesSorties() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime troisMois = now.plusMonths(3);

        List<Sortie> sorties = sortieRepository.findBetweenDates(now, troisMois);

        return sorties.stream()
                .map(sortie -> {
                    SortieResponseDTO dto = SortieMapper.toDto(sortie);

                    List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
                    dto.setNombreParticipants(participations.size());
                    dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

                    List<String> participantIds = participations.stream()
                            .map(p -> p.getUtilisateur().getId())
                            .collect(Collectors.toList());
                    dto.setParticipantIds(participantIds);

                    return dto;
                })
                .collect(Collectors.toList());
    }
}