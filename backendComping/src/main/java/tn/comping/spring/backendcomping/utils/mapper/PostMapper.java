package tn.comping.spring.backendcomping.utils.mapper;

import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.dto.PostResponseDTO;
import tn.comping.spring.backendcomping.entities.Interaction;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.repositories.InteractionRepository;
import tn.comping.spring.backendcomping.repositories.SignupRepository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final SignupRepository signupRepository;
    private final InteractionRepository interactionRepository;

    public PostResponseDTO toResponseDTO(Post post) {
        return toResponseDTO(post, null);
    }

    public PostResponseDTO toResponseDTO(Post post, String currentUserId) {
        var userOpt = signupRepository.findByEmail(post.getAuteurId())
                .or(() -> signupRepository.findById(post.getAuteurId()));

        String auteurNom = userOpt
                .map(user -> {
                    String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
                    String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
                    String fullName = (firstName + " " + lastName).trim();
                    return fullName.isEmpty() ? user.getEmail() : fullName;
                })
                .orElse("Utilisateur");

        String auteurPhoto = userOpt.map(u -> u.getPhoto()).orElse(null);

        // Détecter si l'utilisateur courant a réagi
        boolean likedByCurrentUser = false;
        String myReaction = null;
        if (currentUserId != null) {
            Optional<Interaction> myInteraction = interactionRepository
                    .findByAuteurIdAndCibleTypeAndCibleIdAndType(currentUserId, "POST", post.getId(), "REACTION");
            if (myInteraction.isPresent()) {
                myReaction = myInteraction.get().getEmoji();
                likedByCurrentUser = "👍".equals(myReaction);
            } else {
                // Fallback: ancien type LIKE
                Optional<Interaction> legacyLike = interactionRepository
                        .findByAuteurIdAndCibleTypeAndCibleIdAndType(currentUserId, "POST", post.getId(), "LIKE");
                likedByCurrentUser = legacyLike.isPresent();
            }
        }

        return PostResponseDTO.builder()
                .id(post.getId())
                .auteurId(post.getAuteurId())
                .auteurNom(auteurNom)
                .auteurPhoto(auteurPhoto)
                .typePost(post.getTypePost())
                .avisId(post.getAvisId())
                .cibleType(post.getCibleType())
                .cibleId(post.getCibleId())
                .contenu(post.getContenu())
                .images(post.getImages())
                .datePublication(post.getDatePublication())
                .likesCount(post.getLikesCount())
                .commentairesCount(post.getCommentairesCount())
                .likedByCurrentUser(likedByCurrentUser)
                .reactions(post.getReactions())
                .myReaction(myReaction)
                .hashtags(post.getHashtags())
                .trendScore(post.getTrendScore())
                .visibilite(post.getVisibilite())
                .build();
    }
}
