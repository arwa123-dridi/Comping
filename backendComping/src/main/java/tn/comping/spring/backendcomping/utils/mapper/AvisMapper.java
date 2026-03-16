package tn.comping.spring.backendcomping.utils.mapper;

<<<<<<< HEAD
import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.*;
import tn.comping.spring.backendcomping.entities.Avis;

@Component
public class AvisMapper {
    public Avis toEntity(AvisRequest req) {
        Avis a = new Avis();
        a.setSiteCampingId(req.getSiteCampingId());
        a.setUtilisateurId(req.getUtilisateurId());
        a.setNote(req.getNote());
        a.setCommentaire(req.getCommentaire());
        a.setItineraire(req.getItineraire());
        a.setConvention(req.getConvention());
        return a;
    }

    public AvisResponse toResponse(Avis a) {
        return new AvisResponse(
            a.getId(), a.getSiteCampingId(), a.getUtilisateurId(),
            a.getNote(), a.getCommentaire(),
            a.getDateCreation(), a.getStatutModeration()
        );
=======
import tn.comping.spring.backendcomping.dto.AvisRequestDTO;
import tn.comping.spring.backendcomping.dto.AvisResponseDTO;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.StatutAvis;

import java.util.Date;

/**
 * Mapper pour convertir entre Avis Entity et DTOs
 */
public class AvisMapper {

    /**
     * Convertir AvisRequestDTO vers Avis Entity
     * Utilisé lors de la création d'un nouvel avis
     */
    public static Avis toEntity(AvisRequestDTO dto, String utilisateurId) {
        if (dto == null) {
            return null;
        }

        return Avis.builder()
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .datePublication(new Date())
                .statut(StatutAvis.EN_ATTENTE)
                .valide(false)
                .utilisateurId(utilisateurId)
                .cibleId(dto.getCibleId())
                .typeCible(dto.getTypeCible())
                .build();
    }

    /**
     * Convertir Avis Entity vers AvisResponseDTO
     * Utilisé pour renvoyer les données au client
     */
    public static AvisResponseDTO toResponseDTO(Avis avis, String
utilisateurNom) {
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
                .reponse(null) // Sera ajouté séparément si existe
                .build();
    }

    /**
     * Mettre à jour une entité Avis existante avec les données du DTO
     */
    public static void updateEntityFromDTO(Avis avis, AvisRequestDTO dto) {
        if (avis == null || dto == null) {
            return;
        }

        avis.setNote(dto.getNote());
        avis.setCommentaire(dto.getCommentaire());
        avis.setDateModification(new Date());
        avis.setStatut(StatutAvis.EN_ATTENTE);
        avis.setValide(false);
>>>>>>> origin/mariem-sellami
    }
}