package tn.comping.spring.backendcomping.utils.mapper;


import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;

import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;

import java.util.Date;

public class AvisMapper {

    public static Avis toEntity(AvisRequestDTO dto, String utilisateurId) {
        if (dto == null) return null;

        return Avis.builder()
                .note(dto.getNote())
                .commentaire(dto.getCommentaire().trim())
                .datePublication(new Date())
                .statut(StatutAvis.EN_ATTENTE)
                .valide(false)
                .utilisateurId(utilisateurId)
                .cibleId(dto.getCibleId())
                .typeCible(dto.getTypeCible())
                .parentAvisId(dto.getParentAvisId())
                .build();
    }


    /**
     * Convertir Avis Entity vers AvisResponseDTO
     * Utilisé pour renvoyer les données au client
     */
    public static AvisResponseDTO toResponseDTO(Avis avis, String utilisateurNom) {
        if (avis == null) {
            return null;
        }


        return AvisResponseDTO.builder()
                .id(avis.getId())
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .datePublication(avis.getDatePublication())
                .statut(avis.getStatut())
                .valide(avis.isValide())
                .utilisateurId(avis.getUtilisateurId())
                .utilisateurNom(utilisateurNom)
                .cibleId(avis.getCibleId())
                .typeCible(avis.getTypeCible())
                .dateModification(avis.getDateModification())
                .parentAvisId(avis.getParentAvisId())
                .build();
    }

    public static void updateEntityFromDTO(Avis avis, AvisRequestDTO dto) {
        if (avis == null || dto == null) return;

        avis.setNote(dto.getNote());
        avis.setCommentaire(dto.getCommentaire().trim());
        avis.setDateModification(new Date());
        avis.setStatut(StatutAvis.EN_ATTENTE);
        avis.setValide(false);
    }
}
