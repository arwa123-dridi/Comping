package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.entities.*;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.utils.mapper.SortieMapper;

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

    // ===================== CRUD SORTIE =====================

    @Override
    public SortieResponseDTO createSortie(SortieRequestDTO dto) {
        log.info("Création d'une sortie par l'organisateur: {}", dto.getOrganisateurId());

        SignupEntity organisateur = signupRepository.findById(dto.getOrganisateurId())
                .orElseThrow(() -> new RuntimeException("Organisateur non trouvé"));

        if (organisateur.getRole() != Role.ORGANISATEUR && organisateur.getRole() != Role.ADMIN) {
            throw new RuntimeException("Seuls les organisateurs peuvent créer des sorties");
        }

        Sortie sortie = SortieMapper.toEntity(dto);
        sortie.setOrganisateur(organisateur);

        if (dto.getEquipeId() != null) {
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
            if (!equipe.getOrganisateur().getId().equals(dto.getOrganisateurId())) {
                throw new RuntimeException("Vous ne pouvez pas lier une équipe dont vous n'êtes pas l'organisateur");
            }
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
        enrichSortieDto(dto, sortie);
        return dto;
    }

    /**
     * Enrichit le DTO avec les informations calculées (participants, places)
     * Version robuste contre les nulls et les IDs invalides.
     */
    private void enrichSortieDto(SortieResponseDTO dto, Sortie sortie) {
        if (dto == null || sortie == null || sortie.getId() == null) {
            log.warn("Impossible d'enrichir la sortie: paramètres invalides");
            return;
        }
        try {
            List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
            int count = (participations != null) ? participations.size() : 0;
            dto.setNombreParticipants(count);
            int capacite = (sortie.getCapaciteMax() != null) ? sortie.getCapaciteMax() : 0;
            dto.setPlacesDisponibles(Math.max(0, capacite - count));

            List<String> participantIds = (participations == null) ? new ArrayList<>() :
                    participations.stream()
                            .filter(p -> p != null && p.getUtilisateur() != null && p.getUtilisateur().getId() != null)
                            .map(p -> p.getUtilisateur().getId())
                            .collect(Collectors.toList());
            dto.setParticipantIds(participantIds);
        } catch (Exception e) {
            log.error("Erreur lors de l'enrichissement de la sortie {}: {}", sortie.getId(), e.getMessage());
            // On met des valeurs par défaut pour ne pas casser le frontend
            dto.setNombreParticipants(0);
            dto.setPlacesDisponibles(sortie.getCapaciteMax() != null ? sortie.getCapaciteMax() : 0);
            dto.setParticipantIds(new ArrayList<>());
        }
    }

    @Override
    public List<SortieResponseDTO> getAllSorties() {
        List<Sortie> sorties = sortieRepository.findAll();
        List<SortieResponseDTO> result = new ArrayList<>();

        for (Sortie sortie : sorties) {
            try {
                SortieResponseDTO dto = SortieMapper.toDto(sortie);
                enrichSortieDto(dto, sortie);
                result.add(dto);
            } catch (Exception e) {
                log.error("Sortie ignorée (id: {}, titre: {}): {}",
                        sortie.getId(), sortie.getTitre(), e.getMessage());
            }
        }
        log.info("{} sorties chargées sur {}", result.size(), sorties.size());
        return result;
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

    @Transactional
    public void deleteSortie(String id) {
        log.info("=== DÉBUT suppression sortie {} ===", id);
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée: " + id));
        log.info("Sortie trouvée : {}", sortie.getTitre());

        // Dissociation
        if (sortie.getEquipe() != null || sortie.getEquipeId() != null) {
            sortie.setEquipe(null);
            sortie.setEquipeId(null);
            sortieRepository.save(sortie);
            log.info("Équipe dissociée");
        }

        // Suppression des participations
        try {
            long count = participationRepository.countBySortieId(id);
            log.info("Nombre de participations à supprimer : {}", count);
            participationRepository.deleteBySortieId(id);
            log.info("Participations supprimées");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des participations", e);
            throw new RuntimeException("Impossible de supprimer les participations: " + e.getMessage(), e);
        }

        // Suppression de la sortie
        sortieRepository.deleteById(id);
        log.info("Sortie supprimée avec succès");
    }

    @Override
    public SortieResponseDTO dissocierEquipe(String id) {
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie introuvable"));
        // Dissocier la référence MongoDB
        sortie.setEquipe(null);
        // Dissocier l'ancien champ equipeId (optionnel mais propre)
        sortie.setEquipeId(null);
        Sortie updated = sortieRepository.save(sortie);
        return SortieMapper.toDto(updated);
    }

    // ===================== GESTION DES PARTICIPANTS =====================

    @Override
    public ParticipationDTO inscrireParticipant(String sortieId, tn.comping.spring.backendcomping.dto.InscriptionRequest request) {
        String utilisateurId = request.getUtilisateurId();
        String utilisateurNom = request.getUtilisateurNom();
        String utilisateurEmail = request.getUtilisateurEmail();
        // Validation
        if (sortieId == null || utilisateurId == null || utilisateurNom == null || utilisateurEmail == null) {
            throw new IllegalArgumentException("Paramètres manquants pour l'inscription");
        }

        log.info("Inscription de l'utilisateur {} à la sortie {}", utilisateurId, sortieId);

        Sortie sortie = sortieRepository.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));
        SignupEntity utilisateur = signupRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si déjà inscrit
        if (participationRepository.findByUtilisateurIdAndSortieId(utilisateurId, sortieId).isPresent()) {
            throw new RuntimeException("Vous êtes déjà inscrit à cette sortie");
        }

        // Vérifier la capacité de la sortie
        long nbInscrits = participationRepository.countBySortieId(sortieId);
        if (nbInscrits >= sortie.getCapaciteMax()) {
            throw new RuntimeException("Cette sortie est complète");
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

        // Mettre à jour la liste des IDs participants dans la sortie
        if (sortie.getParticipantIds() == null) {
            sortie.setParticipantIds(new ArrayList<>());
        }
        sortie.getParticipantIds().add(utilisateurId);
        sortieRepository.save(sortie);

        // === AJOUT AUTOMATIQUE À L'ÉQUIPE ===
        String equipeId = null;
        String equipeNom = null;
        String message = null;

        if (sortie.getEquipe() != null) {
            Equipe equipe = sortie.getEquipe();
            log.info("Sortie liée à l'équipe '{}' (id: {})", equipe.getNom(), equipe.getId());

            boolean dejaMembre = equipe.getMembres() != null && equipe.getMembres().stream()
                    .anyMatch(m -> m != null && m.getId() != null && m.getId().equals(utilisateurId));

            if (dejaMembre) {
                message = "⚠️ Inscription réussie, mais vous étiez déjà membre de l'équipe.";
                log.info("Utilisateur déjà membre de l'équipe {}", equipe.getId());
            } else {
                int max = equipe.getNbMembresMax() == null ? 0 : equipe.getNbMembresMax();
                int size = equipe.getMembres() == null ? 0 : equipe.getMembres().size();

                if (max > 0 && size >= max) {
                    message = "⚠️ Inscription réussie, mais l'équipe est pleine. Vous n'avez pas pu être ajouté(e).";
                    log.warn("Équipe {} pleine ({} membres, max {})", equipe.getId(), size, max);
                } else {
                    if (equipe.getMembres() == null) equipe.setMembres(new ArrayList<>());
                    equipe.getMembres().add(utilisateur);
                    equipeRepository.save(equipe);
                    equipeId = equipe.getId();
                    equipeNom = equipe.getNom();
                    message = "✅ Inscription réussie ! Vous avez été ajouté(e) à l'équipe organisatrice.";
                    log.info("Utilisateur ajouté à l'équipe {}", equipe.getId());
                }
            }
        } else {
            message = "ℹ️ Inscription réussie. Cette sortie n'est associée à aucune équipe.";
            log.info("Sortie sans équipe associée");
        }

        ParticipationDTO dto = SortieMapper.toParticipationDto(saved);
        dto.setEquipeId(equipeId);
        dto.setEquipeNom(equipeNom);
        dto.setMessage(message);  // ✅ transmission au frontend

        return dto;
    }

    @Override
    public void desinscrireParticipant(String sortieId, String utilisateurId) {
        Participation participation = participationRepository
                .findByUtilisateurIdAndSortieId(utilisateurId, sortieId)
                .orElseThrow(() -> new RuntimeException("L'utilisateur n'est pas inscrit"));
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
        Sortie sortie = sortieRepository.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));
        Equipe equipe = sortie.getEquipe();

        return participations.stream()
                .map(p -> {
                    ParticipationDTO dto = SortieMapper.toParticipationDto(p);
                    if (equipe != null && equipe.getMembres() != null
                            && equipe.getMembres().contains(p.getUtilisateur().getId())) {
                        dto.setEquipeId(equipe.getId());
                        dto.setEquipeNom(equipe.getNom());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ===================== RECHERCHES SPÉCIFIQUES =====================

    @Override
    public List<SortieResponseDTO> getSortiesByOrganisateur(String organisateurId) {
        List<Sortie> sorties = sortieRepository.findByOrganisateurId(organisateurId);
        List<SortieResponseDTO> result = new ArrayList<>();
        for (Sortie sortie : sorties) {
            try {
                SortieResponseDTO dto = SortieMapper.toDto(sortie);
                enrichSortieDto(dto, sortie);
                result.add(dto);
            } catch (Exception e) {
                log.error("Erreur sur sortie {} pour l'organisateur {}", sortie.getId(), organisateurId, e);
            }
        }
        return result;
    }

    @Override
    public List<SortieResponseDTO> getSortiesByDifficulte(Difficulte difficulte) {
        log.info("Recherche des sorties par difficulté: {}", difficulte);
        List<Sortie> sorties = sortieRepository.findAll().stream()
                .filter(s -> s.getDifficulte() == difficulte)
                .collect(Collectors.toList());
        List<SortieResponseDTO> result = new ArrayList<>();
        for (Sortie sortie : sorties) {
            try {
                SortieResponseDTO dto = SortieMapper.toDto(sortie);
                enrichSortieDto(dto, sortie);
                result.add(dto);
            } catch (Exception e) {
                log.error("Erreur sur sortie {} par difficulté", sortie.getId(), e);
            }
        }
        return result;
    }

    @Override
    public List<SortieResponseDTO> getProchainesSorties() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime troisMois = now.plusMonths(3);
        List<Sortie> sorties = sortieRepository.findBetweenDates(now, troisMois);
        List<SortieResponseDTO> result = new ArrayList<>();
        for (Sortie sortie : sorties) {
            try {
                SortieResponseDTO dto = SortieMapper.toDto(sortie);
                enrichSortieDto(dto, sortie);
                result.add(dto);
            } catch (Exception e) {
                log.error("Erreur sur sortie prochaine {}", sortie.getId(), e);
            }
        }
        return result;
    }
}