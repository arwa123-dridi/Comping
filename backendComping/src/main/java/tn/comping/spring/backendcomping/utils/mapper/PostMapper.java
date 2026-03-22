package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.entities.TypePost;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper Post - Compatible architecture existante
 */
public class PostMapper {

    public static Post toEntity(PostRequestDTO dto, String utilisateurId, String utilisateurEmail) {
        if (dto == null) return null;
        
        return Post.builder()
            .utilisateurId(utilisateurId)
            .utilisateurEmail(utilisateurEmail)
            .contenu(dto.getContenu())
            .images(dto.getImages())
            .typePost(dto.getTypePost())
            .avisId(dto.getAvisId())
            .build();
    }

    public static PostResponseDTO toResponseDTO(Post post) {
        if (post == null) return null;
        
        PostResponseDTO dto = PostResponseDTO.builder()
            .id(post.getId())
            .contenu(post.getContenu())
            .images(post.getImages())
            .typePost(post.getTypePost())
            .utilisateurId(post.getUtilisateurId())
            .utilisateurEmail(post.getUtilisateurEmail())
            .avisId(post.getAvisId())
            .visible(post.isVisible())
            .dateCreation(post.getDateCreation())
            .dateModification(post.getDateModification())
            .build();
            
        dto.setUtilisateurNom("User " + post.getUtilisateurEmail());
        dto.setNombreLikes(0L);
        dto.setDerniersCommentaires(List.of());
        
        return dto;
    }
    
    public static List<PostResponseDTO> toResponseDTOList(List<Post> posts) {
        if (posts == null) return List.of();
        return posts.stream()
            .map(PostMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
}
