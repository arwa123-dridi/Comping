package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.ReponseAvisDTO;
import tn.comping.spring.backendcomping.dto.ReponseAvisRequestDTO;
import tn.comping.spring.backendcomping.entities.ReponseAvis;

import java.util.Date;

/**
 * Mapper pour convertir entre ReponseAvis Entity et DTOs
 */
public class ReponseAvisMapper {

    /**
     * Convertir ReponseAvisRequestDTO vers ReponseAvis Entity
     */
    public static ReponseAvis toEntity(ReponseAvisRequestDTO dto,
                                       String avisId,
                                       String auteurId,
                                       String roleAuteur) {
        if (dto == null) {
            return null;
        }

        return ReponseAvis.builder()
                .contenu(dto.getContenu())
                .dateReponse(new Date())
                .avisId(avisId)
                .auteurId(auteurId)
                .roleAuteur(roleAuteur)
                .build();
    }

    /**
     * Convertir ReponseAvis Entity vers ReponseAvisDTO
     */
    public static ReponseAvisDTO toDTO(ReponseAvis reponse, String auteurNom) {
        if (reponse == null) {
            return null;
        }

        return ReponseAvisDTO.builder()
                .id(reponse.getId())
                .contenu(reponse.getContenu())
                .dateReponse(reponse.getDateReponse())
                .auteurId(reponse.getAuteurId())
                .auteurNom(auteurNom)
                .build();
    }
}