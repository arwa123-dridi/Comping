package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.dto.MembreDTO;
import tn.comping.spring.backendcomping.entities.Equipe;
//import tn.comping.spring.backendcomping.entities.SignupEntity; // À importer après fusion
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EquipeMapper {

    /**
     * Convertit un EquipeRequestDTO en entité Equipe
     */
    public static Equipe toEntity(EquipeRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Equipe equipe = new Equipe();
        equipe.setNom(dto.getNom());
        equipe.setDescription(dto.getDescription());
        equipe.setNbMembresMax(dto.getNbMembresMax() != null ? dto.getNbMembresMax() : 10);
        equipe.setNiveau(dto.getNiveau());
        equipe.setOrganisateurId(dto.getOrganisateurId());
        equipe.setOrganisateurNom(dto.getOrganisateurNom());
        equipe.setDateCreation(LocalDateTime.now());

        // Initialiser la liste des membres avec l'organisateur
        List<String> membreIds = new ArrayList<>();
        membreIds.add(dto.getOrganisateurId());
        equipe.setMembreIds(membreIds);

        return equipe;
    }

    /**
     * Convertit une entité Equipe en EquipeResponseDTO (version simple)
     */
    public static EquipeResponseDTO toDto(Equipe entity) {
        if (entity == null) {
            return null;
        }

        EquipeResponseDTO dto = new EquipeResponseDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setDescription(entity.getDescription());
        dto.setDateCreation(entity.getDateCreation());
        dto.setNbMembresMax(entity.getNbMembresMax());
        dto.setNbMembresActuels(entity.getMembreIds() != null ? entity.getMembreIds().size() : 0);
        dto.setNiveau(entity.getNiveau());
        dto.setOrganisateurId(entity.getOrganisateurId());
        dto.setOrganisateurNom(entity.getOrganisateurNom());

        return dto;
    }

    /**
     * Met à jour une entité Equipe existante avec les données du DTO
     */
    public static Equipe updateEntity(Equipe entity, EquipeRequestDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setNom(dto.getNom());
        entity.setDescription(dto.getDescription());
        if (dto.getNbMembresMax() != null) {
            entity.setNbMembresMax(dto.getNbMembresMax());
        }
        entity.setNiveau(dto.getNiveau());
        entity.setDateModification(LocalDateTime.now());

        return entity;
    }

    /**
     * Convertit une liste de Equipe en liste de EquipeResponseDTO
     */
    public static List<EquipeResponseDTO> toDtoList(List<Equipe> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(EquipeMapper::toDto)
                .collect(Collectors.toList());
    }

     /**
     * Convertit une liste d'utilisateurs en liste de MembreDTO
     * À utiliser APRÈS avoir fusionné avec la branche d'Arwa
     */
//    public static List<MembreDTO> toMembreDtoList(List<SignupEntity> utilisateurs, String organisateurId) {
//        if (utilisateurs == null) {
//            return new ArrayList<>();
//        }
//
//        return utilisateurs.stream()
//                .map(user -> {
//                    MembreDTO membre = new MembreDTO();
//                    membre.setId(user.getId());
//                    membre.setNom(user.getName());
//                    membre.setEmail(user.getEmail());
//                    membre.setEstOrganisateur(user.getId().equals(organisateurId));
//                    return membre;
//                })
//                .collect(Collectors.toList());
//    }
//
//
    /**
     * Version simplifiée sans SignupEntity (avant fusion)
     */
    public static List<MembreDTO> toSimpleMembreDtoList(List<String> membreIds, String organisateurId) {
        if (membreIds == null) {
            return new ArrayList<>();
        }
        return membreIds.stream()
                .map(id -> {
                    MembreDTO membre = new MembreDTO();
                    membre.setId(id);
                    membre.setNom("Utilisateur " + id); // Temporaire
                    membre.setEmail("email@exemple.com"); // Temporaire
                    membre.setEstOrganisateur(id.equals(organisateurId));
                    return membre;
                })
                .collect(Collectors.toList());
    }
}