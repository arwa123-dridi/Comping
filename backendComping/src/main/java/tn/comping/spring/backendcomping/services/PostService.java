package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.*;
import java.util.List;

public interface PostService {
    // CRUD
    PostResponseDTO createPost(PostRequestDTO dto, String userId);
    List<PostResponseDTO> getFeedPosts(int page, int size, String currentUserId);
    List<PostResponseDTO> getUserPosts(String userId, int page, int size, String currentUserId);
    PostResponseDTO getPostById(String id, String currentUserId);
    PostResponseDTO updatePost(String id, PostRequestDTO dto, String userId);
    void deletePost(String id, String userId);

    // Reactions
    void likePost(String postId, String userId);
    void unlikePost(String postId, String userId);
    void reactToPost(String postId, String userId, String emoji);
    void removeReaction(String postId, String userId);

    // Trending & IA
    List<PostResponseDTO> getTrendingPosts(int page, int size, String currentUserId);
    List<PostResponseDTO> getPostsByHashtag(String hashtag, int page, int size, String currentUserId);
    void recalculateTrendScores(); // appelé périodiquement par scheduled task
}
