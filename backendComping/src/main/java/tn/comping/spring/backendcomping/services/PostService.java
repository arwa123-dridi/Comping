package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.PostRequestDTO;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import java.util.List;

public interface PostService {
    PostResponseDTO createPost(PostRequestDTO dto, String userId);
    List<PostResponseDTO> getFeedPosts(int page, int size);
    List<PostResponseDTO> getUserPosts(String userId, int page, int size);
    PostResponseDTO getPostById(String id);
    PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId);
    void deletePost(String id, String userId);
    void likePost(String postId, String userId);
    void unlikePost(String postId, String userId);
}
