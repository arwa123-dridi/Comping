package tn.comping.spring.backendcomping.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.comping.spring.backendcomping.entities.Post;
import tn.comping.spring.backendcomping.entities.TypePost;
import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    // Posts d'un utilisateur
    List<Post> findByUtilisateurIdAndVisibleOrderByDateCreationDesc(String utilisateurId, boolean visible);
    
    // Posts publics récents
    List<Post> findByVisibleOrderByDateCreationDesc(boolean visible);
    
    // Posts de type partage avis
    List<Post> findByTypePostAndAvisIdAndVisibleOrderByDateCreationDesc(TypePost typePost, String avisId, boolean visible);
    
    // Compter posts utilisateur
    long countByUtilisateurIdAndVisible(String utilisateurId, boolean visible);
}

