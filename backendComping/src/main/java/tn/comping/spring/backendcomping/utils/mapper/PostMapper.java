package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

@Component
public class PostMapper {
    public PostResponseDTO toResponseDTO(Post post) {
        String auteurNom = "Utilisateur";
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
