package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Avis;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.entities.TypePost;
import tn.comping.spring.backendcomping.repositories.AvisRepository;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.PostService;
import tn.comping.spring.backendcomping.utils.mapper.PostMapper;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final AvisRepository avisRepository;
    private final SignupRepository signupRepository;

    @Override
    public PostResponseDTO creerPost(PostRequestDTO dto, String utilisateurEmail) {
        Post post = PostMapper.toEntity(dto, utilisateurEmail, utilisateurEmail);
        post = postRepository.save(post);
        log.info("Post créé ID: {} type: {}", post.getId(), post.getTypePost());
        return PostMapper.toResponseDTO(post);
    }

    @Override
    public PostResponseDTO modifierPost(String id, PostRequestDTO dto, String utilisateurEmail) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouvé"));
        
        if (!post.getUtilisateurId().equals(utilisateurEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }

        post.setContenu(dto.getContenu());
        post.setImages(dto.getImages());
        post.setDateModification(new Date());
        post = postRepository.save(post);
        
        log.info("Post modifié ID: {}", id);
        return PostMapper.toResponseDTO(post);
    }

    @Override
    public void supprimerPost(String id, String utilisateurEmail) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post non trouvé"));
        
        if (!post.getUtilisateurId().equals(utilisateurEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non autorisé");
        }
        
        post.setVisible(false); // Soft delete
        postRepository.save(post);
        log.info("Post supprimé (soft) ID: {}", id);
    }

    @Override
    public List<PostResponseDTO> getPostsUtilisateur(String utilisateurId) {
        List<Post> posts = postRepository.findByUtilisateurIdAndVisibleOrderByDateCreationDesc(utilisateurId, true);
        return PostMapper.toResponseDTOList(posts);
    }

    @Override
    public List<PostResponseDTO> getPostsPublics() {
        List<Post> posts = postRepository.findByVisibleOrderByDateCreationDesc(true);
        return PostMapper.toResponseDTOList(posts);
    }

    @Override
    public PostResponseDTO partagerAvis(String avisId, String utilisateurEmail) {
        Avis avis = avisRepository.findById(avisId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis non trouvé"));

        PostRequestDTO dto = PostRequestDTO.builder()
            .contenu("J'ai partagé cet avis : " + avis.getCommentaire().substring(0, Math.min(100, avis.getCommentaire().length())) + "...")
            .typePost(TypePost.PARTAGE_AVIS)
            .avisId(avisId)
            .build();

        return creerPost(dto, utilisateurEmail);
    }
}

