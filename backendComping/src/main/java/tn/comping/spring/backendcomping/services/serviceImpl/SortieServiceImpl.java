package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.Participation;
import tn.comping.spring.backendcomping.entities.Equipe;
import tn.comping.spring.backendcomping.enums.Difficulte;
import tn.comping.spring.backendcomping.enums.StatutSortie;
import tn.comping.spring.backendcomping.repositories.SortieRepository;
import tn.comping.spring.backendcomping.repositories.ParticipationRepository;
import tn.comping.spring.backendcomping.repositories.EquipeRepository;
import tn.comping.spring.backendcomping.services.interfaces.ISortieService;
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

    @Override
    public SortieResponseDTO createSortie(SortieRequestDTO dto) {
        log.info("Création d'une sortie par l'organisateur: {}", dto.getOrganisateurId());

        Sortie sortie = new Sortie();
        sortie.setTitre(dto.getTitre());
        sortie.setDescription(dto.getDescription());
        sortie.setDateDebut(dto.getDateDebut());
        sortie.setDateFin(dto.getDateFin());
        sortie.setLieuDepart(dto.getLieuDepart());
        sortie.setRegion(dto.getRegion());
        sortie.setDifficulte(dto.getDifficulte());
        sortie.setCapaciteMax(dto.getCapaciteMax());
        sortie.setPrixParPersonne(dto.getPrixParPersonne());
        sortie.setEquipementRequis(dto.getEquipementRequis());
        sortie.setAssistanceMedicale(dto.getAssistanceMedicale());
        sortie.setStatut(StatutSortie.PLANIFIEE);

        // IDs simples
        sortie.setOrganisateurId(dto.getOrganisateurId());
        sortie.setOrganisateurNom(dto.getOrganisateurNom());

        // Lier à une équipe si spécifiée
        if (dto.getEquipeId() != null && !dto.getEquipeId().isEmpty()) {
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
            sortie.setEquipeId(equipe.getId());
            sortie.setEquipeNom(equipe.getNom());
        }

        sortie.setDateCreation(LocalDateTime.now());
        sortie.setParticipantIds(new ArrayList<>());

        Sortie saved = sortieRepository.save(sortie);
        log.info("Sortie créée avec ID: {}", saved.getId());

        return mapToResponseDTO(saved);
    }

    @Override
    public SortieResponseDTO getSortieById(String id) {
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée avec l'id: " + id));
        return mapToResponseDTO(sortie);
    }

    @Override
    public List<SortieResponseDTO> getAllSorties() {
        return sortieRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SortieResponseDTO updateSortie(String id, SortieRequestDTO dto) {
        Sortie sortie = sortieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));

        sortie.setTitre(dto.getTitre());
        sortie.setDescription(dto.getDescription());
        sortie.setDateDebut(dto.getDateDebut());
        sortie.setDateFin(dto.getDateFin());
        sortie.setLieuDepart(dto.getLieuDepart());
        sortie.setRegion(dto.getRegion());
        sortie.setDifficulte(dto.getDifficulte());
        sortie.setCapaciteMax(dto.getCapaciteMax());
        sortie.setPrixParPersonne(dto.getPrixParPersonne());
        sortie.setEquipementRequis(dto.getEquipementRequis());
        sortie.setAssistanceMedicale(dto.getAssistanceMedicale());

        if (dto.getEquipeId() != null && !dto.getEquipeId().isEmpty()) {
            Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
            sortie.setEquipeId(equipe.getId());
            sortie.setEquipeNom(equipe.getNom());
        }

        sortie.setDateModification(LocalDateTime.now());

        Sortie updated = sortieRepository.save(sortie);
        return mapToResponseDTO(updated);
    }

    @Override
    public void deleteSortie(String id) {
        log.info("Suppression de la sortie: {}", id);

        // CASCADE: Supprimer toutes les participations liées
        participationRepository.deleteBySortieId(id);

        // Puis supprimer la sortie
        sortieRepository.deleteById(id);

        log.info("Sortie {} supprimée avec toutes ses participations", id);
    }

    @Override
    public ParticipationDTO inscrireParticipant(String sortieId, String utilisateurId, String utilisateurNom, String utilisateurEmail) {
        log.info("Inscription de l'utilisateur {} à la sortie {}", utilisateurId, sortieId);

        Sortie sortie = sortieRepository.findById(sortieId)
                .orElseThrow(() -> new RuntimeException("Sortie non trouvée"));

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
                .utilisateurId(utilisateurId)
                .utilisateurNom(utilisateurNom)
                .utilisateurEmail(utilisateurEmail)
                .sortieId(sortieId)
                .sortieTitre(sortie.getTitre())
                .dateInscription(LocalDateTime.now())
                .statutPresence("CONFIRME")
                .aValideChecklist(false)
                .dateCreation(LocalDateTime.now())
                .build();

        Participation saved = participationRepository.save(participation);

        // Ajouter l'utilisateur à la liste des participants de la sortie
        if(sortie.getParticipantIds()==null){
            sortie.setParticipantIds(new ArrayList<>());
        }
        sortie.getParticipantIds().add(utilisateurId);
        sortieRepository.save(sortie);

        return mapToParticipationDTO(saved);
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
        return participationRepository.findBySortieId(sortieId).stream()
                .map(this::mapToParticipationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SortieResponseDTO> getSortiesByOrganisateur(String organisateurId) {
        return sortieRepository.findByOrganisateurId(organisateurId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


        @Override
        public List<SortieResponseDTO> getSortiesByDifficulte(Difficulte difficulte) {
            log.info("Recherche des sorties par difficulté: {}", difficulte);
            return sortieRepository.findAll().stream()
                    .filter(s -> s.getDifficulte() == difficulte)
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        }
    @Override
    public List<SortieResponseDTO> getProchainesSorties() {
        return sortieRepository.findBetweenDates(LocalDateTime.now(), LocalDateTime.now().plusMonths(3))
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private SortieResponseDTO mapToResponseDTO(Sortie sortie) {
        SortieResponseDTO dto = new SortieResponseDTO();
        dto.setId(sortie.getId());
        dto.setTitre(sortie.getTitre());
        dto.setDescription(sortie.getDescription());
        dto.setDateDebut(sortie.getDateDebut());
        dto.setDateFin(sortie.getDateFin());
        dto.setLieuDepart(sortie.getLieuDepart());
        dto.setRegion(sortie.getRegion());
        dto.setDifficulte(sortie.getDifficulte());
        dto.setCapaciteMax(sortie.getCapaciteMax());
        dto.setPrixParPersonne(sortie.getPrixParPersonne());
        dto.setStatut(sortie.getStatut());

        dto.setOrganisateurId(sortie.getOrganisateurId());
        dto.setOrganisateurNom(sortie.getOrganisateurNom());
        dto.setEquipeId(sortie.getEquipeId());
        dto.setEquipeNom(sortie.getEquipeNom());

        List<Participation> participations = participationRepository.findBySortieId(sortie.getId());
        dto.setNombreParticipants(participations.size());
        dto.setPlacesDisponibles(sortie.getCapaciteMax() - participations.size());

        List<String> participantIds = participations.stream()
                .map(p -> p.getUtilisateurId())  // ← Juste l'ID
                .collect(Collectors.toList());
        dto.setParticipantIds(participantIds);

        dto.setDateCreation(sortie.getDateCreation());

        return dto;
    }

    private ParticipationDTO mapToParticipationDTO(Participation participation) {
        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(participation.getId());
        dto.setUtilisateurId(participation.getUtilisateurId());
        dto.setUtilisateurNom(participation.getUtilisateurNom());
        dto.setSortieId(participation.getSortieId());
        dto.setSortieTitre(participation.getSortieTitre());
        dto.setDateInscription(participation.getDateInscription());
        dto.setStatutPresence(participation.getStatutPresence());
        dto.setAValideChecklist(participation.getAValideChecklist());
        return dto;
    }
}