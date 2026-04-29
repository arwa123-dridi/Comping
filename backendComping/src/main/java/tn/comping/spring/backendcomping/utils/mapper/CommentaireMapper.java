package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.entities.Commentaire;

@Component
public class CommentaireMapper {
    public CommentaireResponseDTO toResponseDTO(Commentaire commentaire) {
        return CommentaireResponseDTO.builder()
                .id(commentaire.getId())
                .postId(commentaire.getPostId())
                .parentCommentId(commentaire.getParentCommentId())
                .auteurId(commentaire.getAuteurId())
                .auteurNom("Utilisateur") // À implémenter avec SignupService
                .contenu(commentaire.getContenu())
                .datePublication(commentaire.getDatePublication())
                .likesCount(commentaire.getLikesCount())
                .niveau(commentaire.getNiveau())
                .build();
    }
}
