package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;
import tn.comping.spring.backendcomping.services.PostService;
import tn.comping.spring.backendcomping.utils.mapper.PostMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SignupRepository signupRepository;
    private final PostMapper postMapper;

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto, String userId) {
        Post post = Post.builder()
                .auteurId(userId)
                .typePost("FEED")
                .avisId(dto.getAvisId())
                .cibleType(dto.getCibleType())
                .cibleId(dto.getCibleId())
                .contenu(dto.getContenu())
                .images(dto.getImages() != null ? dto.getImages() : List.of())
                .datePublication(new Date())
                .likesCount(0)
                .commentairesCount(0)
                .build();

        Post saved = postRepository.save(post);
        log.info("Post créé - ID: {}, Auteur: {}", saved.getId(), userId);
        return postMapper.toResponseDTO(saved);
    }

    @Override
    public List<PostResponseDTO> getFeedPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("datePublication").descending());
        return postRepository.findAll(pageable).stream()
                .map(postMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

@Override
    public List<PostResponseDTO> getUserPosts(String userId, int page, int size) {
        List<Post> posts = postRepository.findByAuteurIdOrderByDatePublicationDesc(userId);
        return posts.stream()
                .skip(page * size)
                .limit(size)
                .map(postMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPostById(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));
        return postMapper.toResponseDTO(post);
    }

    @Override
    public PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));

        if (!post.getAuteurId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        post.setContenu(dto.getContenu());
        post.setImages(dto.getImages() != null ? dto.getImages() : List.of());
        Post updated = postRepository.save(post);
        return postMapper.toResponseDTO(updated);
    }

    @Override
    public void deletePost(String id, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));

        if (!post.getAuteurId().equals(userId)) {
            throw new RuntimeException("Non autorisé");
        }

        postRepository.deleteById(id);
    }

    @Override
    public void likePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));
        post.setLikesCount(post.getLikesCount() + 1);
        postRepository.save(post);
    }

    @Override
    public void unlikePost(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));
        if (post.getLikesCount() > 0) {
            post.setLikesCount(post.getLikesCount() - 1);
        }
        postRepository.save(post);
    }
}
