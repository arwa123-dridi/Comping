package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.InteractionRequestDTO;
import tn.comping.spring.backendcomping.dto.InteractionResponseDTO;
import tn.comping.spring.backendcomping.entities.CibleType;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.TypeInteraction;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper statique pour Interaction Entity ↔ DTO
 * Compatible architecture existante (AvisMapper)
 */
public class InteractionMapper {

    private static SignupRepository signupRepository; // Injection pour noms utilisateurs

    /**
     * Créer Interaction depuis RequestDTO + utilisateur connecté
     */
public static Interaction toEntity(InteractionRequestDTO dto, String utilisateurId, 
                                     String utilisateurEmail, TypeInteraction type) {
        return Interaction.builder()
            .utilisateurId(utilisateurId)
            .utilisateurEmail(utilisateurEmail)
            .type(type)
            .cibleType(dto.getCibleType())
            .cibleId(dto.getCibleId())
            .contenu(dto.getContenu())
            .build();
    }
    
    /**
     * Convertir Entity vers ResponseDTO avec nom utilisateur
     */
    public static InteractionResponseDTO toResponseDTO(Interaction interaction) {
        if (interaction == null) return null;
        
        InteractionResponseDTO dto = InteractionResponseDTO.builder()
            .id(interaction.getId())
            .type(interaction.getType())
            .utilisateurId(interaction.getUtilisateurId())
            .utilisateurEmail(interaction.getUtilisateurEmail())
            .cibleType(interaction.getCibleType())
            .cibleId(interaction.getCibleId())
            .contenu(interaction.getContenu())
            .dateInteraction(interaction.getDateInteraction())
            .visible(interaction.isVisible())
            .build();
            
        // TODO: Récupérer nom via SignupRepository.findByEmail
        dto.setUtilisateurNom("Utilisateur " + interaction.getUtilisateurEmail()); // Placeholder
        
        return dto;
    }
    
    public static List<InteractionResponseDTO> toResponseDTOList(List<Interaction> interactions) {
        return interactions.stream()
                .map(InteractionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}

