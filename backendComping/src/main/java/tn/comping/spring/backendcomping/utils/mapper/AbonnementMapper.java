package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.AbonnementResponseDTO;
import tn.comping.spring.backendcomping.entities.Abonnement;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper Abonnement - PHASE 4
 */
public class AbonnementMapper {

    public static AbonnementResponseDTO toResponseDTO(Abonnement abonnement) {
        if (abonnement == null) return null;
        
        AbonnementResponseDTO dto = AbonnementResponseDTO.builder()
            .id(abonnement.getId())
            .suiveurId(abonnement.getSuiveurId())
            .suiviId(abonnement.getSuiviId())
            .dateAbonnement(abonnement.getDateAbonnement())
            .build();
            
        // TODO: Noms via SignupRepository
        dto.setSuiveurNom("User " + abonnement.getSuiveurId());
        dto.setSuiviNom("User " + abonnement.getSuiviId());
        
        return dto;
    }
    
    public static List<AbonnementResponseDTO> toResponseDTOList(List<Abonnement> abonnements) {
        return abonnements.stream()
            .map(AbonnementMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
}

