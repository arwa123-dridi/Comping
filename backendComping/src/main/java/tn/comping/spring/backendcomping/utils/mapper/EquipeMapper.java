package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.EquipeRequestDTO;
import tn.comping.spring.backendcomping.dto.EquipeResponseDTO;
import tn.comping.spring.backendcomping.dto.MembreDTO;
import tn.comping.spring.backendcomping.entities.Equipe;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EquipeMapper {

    public static Equipe toEntity(EquipeRequestDTO dto) {
        if (dto == null) return null;

        Equipe equipe = new Equipe();
        equipe.setNom(dto.getNom());
        equipe.setDescription(dto.getDescription());
        equipe.setNbMembresMax(dto.getNbMembresMax() != null ? dto.getNbMembresMax() : 10);
        equipe.setNiveau(dto.getNiveau());
        equipe.setDateCreation(LocalDateTime.now());
        return equipe;
    }

    public static EquipeResponseDTO toDto(Equipe entity) {
        if (entity == null) return null;

        EquipeResponseDTO dto = new EquipeResponseDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setDescription(entity.getDescription());
        dto.setDateCreation(entity.getDateCreation());
        dto.setNbMembresMax(entity.getNbMembresMax());
        dto.setNiveau(entity.getNiveau());

        if (entity.getOrganisateur() != null) {
            dto.setOrganisateurId(entity.getOrganisateur().getId());
            dto.setOrganisateurNom(entity.getOrganisateur().getLastName());
        }

        if (entity.getMembres() != null) {

            // ✅ FIX : filtrer les membres null (@DBRef brisé en MongoDB)
            List<MembreDTO> membres = entity.getMembres()
                    .stream()
                    .filter(user -> user != null && user.getId() != null) // ← ligne ajoutée
                    .map(user -> {
                        MembreDTO m = new MembreDTO();
                        m.setId(user.getId());
                        m.setPrenom(user.getFirstName());
                        m.setNom(user.getLastName());
                        m.setEmail(user.getEmail());

                        if (entity.getOrganisateur() != null) {
                            m.setEstOrganisateur(
                                    user.getId().equals(entity.getOrganisateur().getId())
                            );
                        }
                        return m;
                    })
                    .collect(Collectors.toList());

            dto.setMembres(membres);
            dto.setNbMembresActuels(membres.size()); // ← compte les membres valides seulement
        } else {
            dto.setMembres(new ArrayList<>());
            dto.setNbMembresActuels(0);
        }

        return dto;
    }

    public static Equipe updateEntity(Equipe entity, EquipeRequestDTO dto) {
        if (entity == null || dto == null) return entity;

        entity.setNom(dto.getNom());
        entity.setDescription(dto.getDescription());
        if (dto.getNbMembresMax() != null) {
            entity.setNbMembresMax(dto.getNbMembresMax());
        }
        entity.setNiveau(dto.getNiveau());
        entity.setDateModification(LocalDateTime.now());
        return entity;
    }

    public static List<EquipeResponseDTO> toDtoList(List<Equipe> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream()
                .map(EquipeMapper::toDto)
                .collect(Collectors.toList());
    }
}