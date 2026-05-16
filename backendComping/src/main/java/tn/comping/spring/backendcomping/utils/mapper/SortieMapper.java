package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.SortieRequestDTO;
import tn.comping.spring.backendcomping.dto.SortieResponseDTO;
import tn.comping.spring.backendcomping.dto.ParticipationDTO;
import tn.comping.spring.backendcomping.entities.Sortie;
import tn.comping.spring.backendcomping.entities.Participation;
import tn.comping.spring.backendcomping.entities.StatutSortie;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SortieMapper {
    /**
     * Convertit un SortieRequestDTO en entité Sortie
     */
    public static Sortie toEntity(SortieRequestDTO dto) {
        if (dto == null) return null;

        return Sortie.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .lieuDepart(dto.getLieuDepart())
                .region(dto.getRegion())
                .difficulte(dto.getDifficulte())
                .capaciteMax(dto.getCapaciteMax())
                .prixParPersonne(dto.getPrixParPersonne())
                .equipementRequis(dto.getEquipementRequis())
                .assistanceMedicale(dto.getAssistanceMedicale())
                .statut(StatutSortie.PLANIFIEE)
                .participantIds(new ArrayList<>())
                .dateCreation(LocalDateTime.now())
                .build();
    }

    /**
     * Convertit une entité Sortie en SortieResponseDTO
     */
    public static SortieResponseDTO toDto(Sortie entity) {
        if (entity == null) return null;

        SortieResponseDTO dto = new SortieResponseDTO();
        dto.setId(entity.getId());
        dto.setTitre(entity.getTitre());
        dto.setDescription(entity.getDescription());
        dto.setDateDebut(entity.getDateDebut());
        dto.setDateFin(entity.getDateFin());
        dto.setLieuDepart(entity.getLieuDepart());
        dto.setRegion(entity.getRegion());
        dto.setDifficulte(entity.getDifficulte());
        dto.setCapaciteMax(entity.getCapaciteMax());
        dto.setPrixParPersonne(entity.getPrixParPersonne());
        dto.setStatut(entity.getStatut());
        dto.setDateCreation(entity.getDateCreation());

        // Informations organisateur (seront remplies par le service)
        if (entity.getOrganisateur() != null) {
            dto.setOrganisateurId(entity.getOrganisateur().getId());
            dto.setOrganisateurPrenom(entity.getOrganisateur().getFirstName());
            dto.setOrganisateurNom(entity.getOrganisateur().getLastName());
        }

        // Informations équipe
        if (entity.getEquipe() != null) {
            dto.setEquipeId(entity.getEquipe().getId());
            dto.setEquipeNom(entity.getEquipe().getNom());
        }

        return dto;
    }

    /**
     * Met à jour une entité Sortie avec les données du DTO
     */
    public static Sortie updateEntity(Sortie entity, SortieRequestDTO dto) {
        if (entity == null || dto == null) return entity;

        entity.setTitre(dto.getTitre());
        entity.setDescription(dto.getDescription());
        entity.setDateDebut(dto.getDateDebut());
        entity.setDateFin(dto.getDateFin());
        entity.setLieuDepart(dto.getLieuDepart());
        entity.setRegion(dto.getRegion());
        entity.setDifficulte(dto.getDifficulte());
        entity.setCapaciteMax(dto.getCapaciteMax());
        entity.setPrixParPersonne(dto.getPrixParPersonne());
        entity.setEquipementRequis(dto.getEquipementRequis());
        entity.setAssistanceMedicale(dto.getAssistanceMedicale());
        entity.setDateModification(LocalDateTime.now());

        return entity;
    }

    /**
     * Convertit une liste de Sortie en liste de SortieResponseDTO
     */
    public static List<SortieResponseDTO> toDtoList(List<Sortie> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream()
                .map(SortieMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une Participation en ParticipationDTO
     */
    public static ParticipationDTO toParticipationDto(Participation participation) {
        if (participation == null) return null;

        ParticipationDTO dto = new ParticipationDTO();
        dto.setId(participation.getId());

        if (participation.getUtilisateur() != null) {
            dto.setUtilisateurId(participation.getUtilisateur().getId());
            dto.setUtilisateurNom(participation.getUtilisateur().getLastName());
            dto.setUtilisateurPrenom(participation.getUtilisateur().getFirstName());
            dto.setUtilisateurEmail(participation.getUtilisateur().getEmail());
        }

        if (participation.getSortie() != null) {
            dto.setSortieId(participation.getSortie().getId());
            dto.setSortieTitre(participation.getSortie().getTitre());
        }

        dto.setDateInscription(participation.getDateInscription());
        dto.setStatutPresence(participation.getStatutPresence());
        dto.setAValideChecklist(participation.getAValideChecklist());

        return dto;
    }
    /**
     * Convertit une liste de Participation en liste de ParticipationDTO
     */
    public static List<ParticipationDTO> toParticipationDtoList(List<Participation> participations) {
        if (participations == null) return new ArrayList<>();
        return participations.stream()
                .map(SortieMapper::toParticipationDto)
                .collect(Collectors.toList());
    }
}