package tn.comping.spring.backendcomping.utils.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.CommentaireResponseDTO;
import tn.comping.spring.backendcomping.entities.Commentaire;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

@Component
@RequiredArgsConstructor
public class CommentaireMapper {

    private final SignupRepository signupRepository;

    public CommentaireResponseDTO toResponseDTO(Commentaire commentaire) {
        String auteurNom = signupRepository.findByEmail(commentaire.getAuteurId())
                .map(user -> {
                    String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
                    String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    return fullName.isEmpty() ? user.getEmail() : fullName;
                })
                .orElse("Utilisateur");

        return CommentaireResponseDTO.builder()
                .id(commentaire.getId())
                .postId(commentaire.getPostId())
                .parentCommentId(commentaire.getParentCommentId())
                .auteurId(commentaire.getAuteurId())
                .auteurNom(auteurNom)
                .contenu(commentaire.getContenu())
                .datePublication(commentaire.getDatePublication())
                .likesCount(commentaire.getLikesCount())
                .niveau(commentaire.getNiveau())
                .build();
    }
}
