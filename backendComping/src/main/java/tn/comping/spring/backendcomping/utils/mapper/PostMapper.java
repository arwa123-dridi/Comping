package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final SignupRepository signupRepository;

    public PostResponseDTO toResponseDTO(Post post) {
        String auteurNom = signupRepository.findByEmail(post.getAuteurId())
                .map(user -> {
                    String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
                    String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    return fullName.isEmpty() ? user.getEmail() : fullName;
                })
                .orElse("Utilisateur");
        return PostResponseDTO.builder()
                .id(post.getId())
                .auteurNom(auteurNom)
                .typePost(post.getTypePost())
                .avisId(post.getAvisId())
                .cibleType(post.getCibleType())
                .cibleId(post.getCibleId())
                .contenu(post.getContenu())
                .images(post.getImages())
                .datePublication(post.getDatePublication())
                .likesCount(post.getLikesCount())
                .commentairesCount(post.getCommentairesCount())
                .build();
    }
}
